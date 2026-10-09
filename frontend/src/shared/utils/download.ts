/** The file name a server suggests in Content-Disposition, or the fallback. */
export function fileNameFrom(contentDisposition: string | undefined | null, fallback: string): string {
  if (!contentDisposition) return fallback;
  const encoded = /filename\*=UTF-8''([^;]+)/i.exec(contentDisposition);
  if (encoded) return decodeURIComponent(encoded[1].replace(/"/g, ""));
  const plain = /filename="?([^";]+)"?/i.exec(contentDisposition);
  return plain ? plain[1] : fallback;
}

/** Saves a downloaded file through a temporary link. */
export function saveBlob(blob: Blob, fileName: string): void {
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}
