// Lint gate that only moves one way: the build fails when ESLint reports more errors than .eslint-baseline.
// The baseline holds the errors the code base already had; when you fix some, lower it (or run with --update),
// so they cannot come back. New code therefore has to be lint-clean.
import { ESLint } from "eslint";
import { readFileSync, writeFileSync } from "node:fs";

const BASELINE_FILE = new URL("../.eslint-baseline", import.meta.url);
const baseline = Number(readFileSync(BASELINE_FILE, "utf8").trim());
const eslint = new ESLint();
const results = await eslint.lintFiles(["."]);
const errors = results.reduce((sum, r) => sum + r.errorCount, 0);

if (process.argv.includes("--update")) {
  writeFileSync(BASELINE_FILE, `${errors}\n`);
  console.log(`Baseline set to ${errors} error(s).`);
} else if (errors > baseline) {
  const formatter = await eslint.loadFormatter("stylish");
  console.log(await formatter.format(results.filter((r) => r.errorCount > 0)));
  console.error(`ESLint: ${errors} error(s), above the baseline of ${baseline}. Fix the new ones.`);
  process.exit(1);
} else if (errors < baseline) {
  console.log(`ESLint: ${errors} error(s), below the baseline of ${baseline}. Lower .eslint-baseline to ${errors}.`);
} else {
  console.log(`ESLint: ${errors} error(s), at the baseline of ${baseline}.`);
}
