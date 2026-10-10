#!/bin/bash
# ===================================================================
# Run shell commands on an EC2 instance through SSM Run Command (no
# SSH, no open port), wait for the end, print the output, and exit
# non-zero unless the commands succeeded.
#
#   ssm-run.sh <instance-id> <comment> <parameters.json> [output-file]
#
# parameters.json: AWS-RunShellScript parameters, e.g.
#   {"commands": ["..."], "executionTimeout": ["1200"]}
# output-file:     where to also save the standard output (optional)
# ===================================================================
set -euo pipefail

INSTANCE_ID="$1"
COMMENT="$2"
PARAMS="$3"
OUTPUT_FILE="${4:-}"

COMMAND_ID=$(aws ssm send-command \
  --instance-ids "$INSTANCE_ID" \
  --document-name AWS-RunShellScript \
  --comment "${COMMENT:0:100}" \
  --parameters "file://$PARAMS" \
  --query Command.CommandId --output text)
echo "SSM command $COMMAND_ID on $INSTANCE_ID"

STATUS=Pending
for _ in $(seq 1 225); do # up to 30 minutes
  sleep 8
  STATUS=$(aws ssm get-command-invocation --command-id "$COMMAND_ID" --instance-id "$INSTANCE_ID" \
    --query Status --output text 2>/dev/null || echo Pending)
  case "$STATUS" in Pending | InProgress | Delayed) continue ;; *) break ;; esac
done

OUTPUT=$(aws ssm get-command-invocation --command-id "$COMMAND_ID" --instance-id "$INSTANCE_ID" \
  --query StandardOutputContent --output text || true)
echo "::group::Output"
echo "$OUTPUT"
echo "::endgroup::"
if [ -n "$OUTPUT_FILE" ]; then
  printf '%s\n' "$OUTPUT" >"$OUTPUT_FILE"
fi

ERRORS=$(aws ssm get-command-invocation --command-id "$COMMAND_ID" --instance-id "$INSTANCE_ID" \
  --query StandardErrorContent --output text || true)
if [ -n "$ERRORS" ] && [ "$ERRORS" != "None" ]; then
  echo "::group::Errors"
  echo "$ERRORS"
  echo "::endgroup::"
fi

if [ "$STATUS" != "Success" ]; then
  echo "::error::$COMMENT finished with status $STATUS"
  exit 1
fi
