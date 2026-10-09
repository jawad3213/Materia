import Badge from "../../../shared/components/ui/badge/Badge";

/** Eyebrow badge, title and lead paragraph opening a section; `data-reveal` children are animated by the section. */
export default function SectionHeading({
  eyebrow,
  title,
  lead,
  align = "center",
}: {
  eyebrow: string;
  title: string;
  lead?: string;
  align?: "center" | "left";
}) {
  const centered = align === "center";
  return (
    <div className={centered ? "mx-auto max-w-3xl text-center" : "max-w-2xl"}>
      <div data-reveal>
        <Badge size="sm" color="primary">
          {eyebrow}
        </Badge>
      </div>
      <h2 data-reveal className="mt-4 text-3xl font-bold tracking-tight text-gray-900 sm:text-title-md dark:text-white">
        {title}
      </h2>
      {lead && (
        <p data-reveal className="mt-4 text-lg leading-relaxed text-gray-500 dark:text-gray-400">
          {lead}
        </p>
      )}
    </div>
  );
}
