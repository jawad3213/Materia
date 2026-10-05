/** Placeholder shown in a chart panel that has nothing to draw yet. */
export default function EmptyChart({ message, height = 260 }: { message: string; height?: number }) {
  return (
    <div
      className="flex items-center justify-center rounded-xl border border-dashed border-gray-200 text-center text-sm text-gray-400 dark:border-gray-800 dark:text-gray-500"
      style={{ height }}
    >
      {message}
    </div>
  );
}
