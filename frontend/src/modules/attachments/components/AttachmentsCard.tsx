import { useCallback, useEffect, useState } from "react";
import { useDropzone, type FileRejection } from "react-dropzone";
import useAuth from "../../auth/hooks/useAuth";
import attachmentService from "../services/attachmentService";
import {
  ACCEPTED_ATTACHMENT_TYPES,
  ATTACHMENT_CATEGORY_LABELS,
  ATTACHMENT_PERMISSIONS,
  MAX_ATTACHMENT_BYTES,
  type Attachment,
  type AttachmentCategory,
  type AttachmentOwnerType,
} from "../types";
import { DetailCard } from "../../../shared/components/page/DetailParts";
import { SELECT_CLASS } from "../../../shared/components/page/pageStyles";
import { getApiErrorMessage } from "../../../shared/utils/apiError";

const paperclipIcon = (
  <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
  </svg>
);

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function saveBlobUrl(url: string, fileName: string) {
  const link = document.createElement("a");
  link.href = url;
  link.download = fileName;
  link.click();
  URL.revokeObjectURL(url);
}

function FileTypeBadge({ contentType }: { contentType: string }) {
  const isPdf = contentType === "application/pdf";
  return (
    <span
      className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-lg text-[10px] font-bold uppercase ${
        isPdf ? "bg-error-50 text-error-600 dark:bg-error-500/10" : "bg-brand-50 text-brand-600 dark:bg-brand-500/10 dark:text-brand-400"
      }`}
    >
      {isPdf ? "PDF" : "IMG"}
    </span>
  );
}

interface AttachmentsCardProps {
  ownerType: AttachmentOwnerType;
  ownerId: string;
  /** Preselected category for new files, e.g. SUPPLIER_INVOICE on an invoice. */
  defaultCategory?: AttachmentCategory;
}

/**
 * Files attached to a document: supplier invoice scans, delivery notes, quality photos.
 * Shown only to users who may read the document; adding needs its write permission, and only
 * the uploader or an administrator may remove a file (AttachmentService.delete on the backend).
 */
export default function AttachmentsCard({ ownerType, ownerId, defaultCategory = "OTHER" }: AttachmentsCardProps) {
  const { user, hasPermission } = useAuth();
  const permissions = ATTACHMENT_PERMISSIONS[ownerType];
  const canRead = hasPermission(permissions.read);
  const canWrite = hasPermission(permissions.write);

  const [attachments, setAttachments] = useState<Attachment[]>([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [busyId, setBusyId] = useState<string | null>(null);
  const [category, setCategory] = useState<AttachmentCategory>(defaultCategory);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!canRead) return;
    let cancelled = false;
    attachmentService
      .list(ownerType, ownerId)
      .then((res) => {
        if (!cancelled) setAttachments(res.data);
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err, "The attachments could not be loaded."));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [canRead, ownerType, ownerId]);

  const onDrop = useCallback(
    async (accepted: File[], rejected: FileRejection[]) => {
      setError(
        rejected.length
          ? `${rejected.map((r) => r.file.name).join(", ")}: only PDF, PNG, JPEG or WebP files up to ${formatFileSize(MAX_ATTACHMENT_BYTES)} can be attached.`
          : null
      );
      if (!accepted.length) return;
      setUploading(true);
      try {
        for (const file of accepted) {
          const res = await attachmentService.upload(ownerType, ownerId, category, file);
          setAttachments((current) => [res.data, ...current]);
        }
      } catch (err) {
        setError(getApiErrorMessage(err, "The file could not be attached."));
      } finally {
        setUploading(false);
      }
    },
    [ownerType, ownerId, category]
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: ACCEPTED_ATTACHMENT_TYPES,
    maxSize: MAX_ATTACHMENT_BYTES,
    disabled: !canWrite || uploading,
  });

  /**
   * Opens the file in a new tab. The tab is opened before the request so the popup blocker allows it;
   * if it is blocked anyway, the file is downloaded instead.
   */
  const handleOpen = async (attachment: Attachment) => {
    const tab = window.open("", "_blank");
    setBusyId(attachment.id);
    try {
      const res = await attachmentService.content(attachment.id);
      const url = URL.createObjectURL(res.data);
      if (tab) {
        tab.location.replace(url);
        setTimeout(() => URL.revokeObjectURL(url), 60_000);
      } else {
        saveBlobUrl(url, attachment.fileName);
      }
    } catch (err) {
      tab?.close();
      setError(getApiErrorMessage(err, "The file could not be opened."));
    } finally {
      setBusyId(null);
    }
  };

  const handleDownload = async (attachment: Attachment) => {
    setBusyId(attachment.id);
    try {
      const res = await attachmentService.content(attachment.id);
      saveBlobUrl(URL.createObjectURL(res.data), attachment.fileName);
    } catch (err) {
      setError(getApiErrorMessage(err, "The file could not be downloaded."));
    } finally {
      setBusyId(null);
    }
  };

  const handleDelete = async (attachment: Attachment) => {
    if (!window.confirm(`Remove ${attachment.fileName}?`)) return;
    setBusyId(attachment.id);
    try {
      await attachmentService.remove(attachment.id);
      setAttachments((current) => current.filter((a) => a.id !== attachment.id));
    } catch (err) {
      setError(getApiErrorMessage(err, "The file could not be removed."));
    } finally {
      setBusyId(null);
    }
  };

  if (!canRead) return null;

  const canRemove = (attachment: Attachment) => canWrite && (user?.role === "ADMIN" || (!!user?.id && user.id === attachment.uploadedBy));

  return (
    <DetailCard title={`Attachments (${attachments.length})`} icon={paperclipIcon}>
      <div className="space-y-4">
        {canWrite && (
          <div className="space-y-3">
            <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
              <label htmlFor={`attachment-category-${ownerId}`} className="text-sm font-medium text-gray-700 dark:text-gray-300">
                File type
              </label>
              <select
                id={`attachment-category-${ownerId}`}
                value={category}
                onChange={(e) => setCategory(e.target.value as AttachmentCategory)}
                className={`${SELECT_CLASS} sm:max-w-xs`}
              >
                {(Object.keys(ATTACHMENT_CATEGORY_LABELS) as AttachmentCategory[]).map((c) => (
                  <option key={c} value={c}>
                    {ATTACHMENT_CATEGORY_LABELS[c]}
                  </option>
                ))}
              </select>
            </div>
            <div
              {...getRootProps()}
              className={`cursor-pointer rounded-xl border border-dashed p-6 text-center transition ${
                isDragActive
                  ? "border-brand-500 bg-brand-50/50 dark:bg-brand-500/5"
                  : "border-gray-300 bg-gray-50 hover:border-brand-500 dark:border-gray-700 dark:bg-gray-900"
              } ${uploading ? "pointer-events-none opacity-60" : ""}`}
            >
              <input {...getInputProps()} aria-label="Attach files" />
              <p className="text-sm font-medium text-gray-800 dark:text-white/90">
                {uploading ? "Uploading..." : isDragActive ? "Drop the files here" : "Drop files here or click to browse"}
              </p>
              <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                PDF, PNG, JPEG or WebP, up to {formatFileSize(MAX_ATTACHMENT_BYTES)} each
              </p>
            </div>
          </div>
        )}

        {error && (
          <p role="alert" className="rounded-lg bg-error-50 px-3 py-2 text-sm text-error-600 dark:bg-error-500/10 dark:text-error-400">
            {error}
          </p>
        )}

        {loading ? (
          <p className="text-sm text-gray-500 dark:text-gray-400">Loading attachments...</p>
        ) : attachments.length === 0 ? (
          <p className="text-sm text-gray-500 dark:text-gray-400">No files attached yet.</p>
        ) : (
          <ul className="divide-y divide-gray-100 dark:divide-white/[0.05]">
            {attachments.map((a) => (
              <li key={a.id} className="flex items-center gap-3 py-3">
                <FileTypeBadge contentType={a.contentType} />
                <div className="min-w-0 flex-1">
                  <button
                    type="button"
                    onClick={() => handleOpen(a)}
                    disabled={busyId === a.id}
                    className="block max-w-full truncate text-left text-sm font-medium text-gray-900 hover:text-brand-500 dark:text-white"
                    title={a.fileName}
                  >
                    {a.fileName}
                  </button>
                  <p className="truncate text-xs text-gray-500 dark:text-gray-400">
                    {ATTACHMENT_CATEGORY_LABELS[a.category]} · {formatFileSize(a.sizeBytes)} · {a.uploadedByName || "Unknown"} ·{" "}
                    {new Date(a.uploadedAt).toLocaleDateString()}
                  </p>
                </div>
                <div className="flex shrink-0 items-center gap-1">
                  <button
                    type="button"
                    onClick={() => handleDownload(a)}
                    disabled={busyId === a.id}
                    className="rounded-md px-2 py-1 text-xs font-medium text-gray-600 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-white/[0.05]"
                  >
                    Download
                  </button>
                  {canRemove(a) && (
                    <button
                      type="button"
                      onClick={() => handleDelete(a)}
                      disabled={busyId === a.id}
                      className="rounded-md px-2 py-1 text-xs font-medium text-error-600 hover:bg-error-50 dark:hover:bg-error-500/10"
                    >
                      Remove
                    </button>
                  )}
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </DetailCard>
  );
}
