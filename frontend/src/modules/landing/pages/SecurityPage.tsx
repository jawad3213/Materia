import { useRef } from "react";
import PageMeta from "../../../shared/components/common/PageMeta";
import { Table, TableBody, TableCell, TableHeader, TableRow } from "../../../shared/components/ui/table";
import { gsap, MOTION_OK, useGSAP } from "../animations/gsapSetup";
import useReveal from "../animations/useReveal";
import { CONTROLS } from "../content/landingContent";
import PageHero from "../components/PageHero";
import SectionHeading from "../components/SectionHeading";
import CtaBanner from "../components/CtaBanner";
import { CHECK, LANDING_ICONS } from "../components/landingIcons";

type Allowed = [admin: boolean, purchaser: boolean, receiver: boolean];

/** What each role may do, as defined in Role.java. */
const MATRIX: { action: string; allowed: Allowed }[] = [
  { action: "Create and submit requisitions", allowed: [true, true, false] },
  { action: "Approve requisitions (never their own)", allowed: [true, true, false] },
  { action: "Create and send purchase orders", allowed: [true, true, false] },
  { action: "Record supplier confirmation", allowed: [true, false, false] },
  { action: "Receive goods (when assigned)", allowed: [true, false, true] },
  { action: "Prepare and ship vendor returns", allowed: [true, true, true] },
  { action: "Record supplier invoices", allowed: [true, true, false] },
  { action: "Verify invoices", allowed: [true, false, false] },
  { action: "Execute payments", allowed: [true, false, false] },
  { action: "View the dashboard", allowed: [true, true, true] },
  { action: "Manage staff and roles", allowed: [true, false, false] },
];

export default function SecurityPage() {
  const root = useRef<HTMLDivElement>(null);
  useReveal(root);

  useGSAP(
    () => {
      const mm = gsap.matchMedia();
      mm.add(MOTION_OK, () => {
        gsap.from("[data-matrix-mark]", {
          scale: 0,
          autoAlpha: 0,
          duration: 0.35,
          stagger: { each: 0.025, from: "start" },
          ease: "back.out(2.5)",
          scrollTrigger: { trigger: "[data-matrix]", start: "top 75%", once: true },
        });
      });
    },
    { scope: root }
  );

  return (
    <div ref={root}>
      <PageMeta title="Security & Controls | Materia" description="Server-side permissions, separation of duties, audit trail and the rules that protect your money." />
      <PageHero
        eyebrow="Security & controls"
        title="Controls that hold, whoever is clicking"
        lead="Materia's rules live on the server. The interface shows each person what their role allows, and the server refuses anything else."
      />

      <section className="mx-auto max-w-7xl px-5 pb-24 sm:px-8">
        <div className="grid grid-cols-1 gap-5 md:grid-cols-2 lg:grid-cols-3">
          {CONTROLS.map((c) => (
            <article key={c.title} data-reveal className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-white/10 dark:bg-white/[0.03]">
              <span className="flex size-12 items-center justify-center rounded-xl bg-brand-50 text-brand-500 dark:bg-brand-500/15">{LANDING_ICONS[c.icon]}</span>
              <h3 className="mt-5 text-lg font-semibold text-gray-900 dark:text-white">{c.title}</h3>
              <p className="mt-2 text-sm leading-relaxed text-gray-500 dark:text-gray-400">{c.text}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="bg-gray-50 py-24 dark:bg-white/[0.02]">
        <div className="mx-auto max-w-5xl px-5 sm:px-8">
          <SectionHeading
            eyebrow="Role permissions"
            title="Who can do what"
            lead="Three roles, each limited to its part of the cycle. The same matrix is enforced on every screen and every endpoint."
          />
          <div data-reveal data-matrix className="mt-12 overflow-hidden rounded-2xl border border-gray-200 bg-white dark:border-white/10 dark:bg-gray-900">
            <div className="max-w-full overflow-x-auto">
              <Table>
                <TableHeader className="border-b border-gray-100 bg-gray-50/50 dark:border-white/[0.05] dark:bg-gray-900/50">
                  <TableRow>
                    <TableCell isHeader className="px-6 py-4 text-left text-sm font-medium text-gray-500 dark:text-gray-400">
                      Action
                    </TableCell>
                    {["Administrator", "Purchaser", "Receiver"].map((role) => (
                      <TableCell key={role} isHeader className="px-4 py-4 text-center text-sm font-medium text-gray-500 dark:text-gray-400">
                        {role}
                      </TableCell>
                    ))}
                  </TableRow>
                </TableHeader>
                <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                  {MATRIX.map((row) => (
                    <TableRow key={row.action}>
                      <TableCell className="px-6 py-3.5 text-sm text-gray-700 dark:text-gray-300">{row.action}</TableCell>
                      {row.allowed.map((ok, i) => (
                        <TableCell key={i} className="px-4 py-3.5 text-center">
                          <span
                            data-matrix-mark
                            className={`inline-flex size-7 items-center justify-center rounded-full ${
                              ok ? "bg-success-500 text-white" : "bg-gray-100 text-gray-300 dark:bg-white/5 dark:text-gray-600"
                            }`}
                            aria-label={ok ? "Allowed" : "Not allowed"}
                          >
                            {ok ? CHECK : "–"}
                          </span>
                        </TableCell>
                      ))}
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </div>
        </div>
      </section>

      <div className="pt-24">
        <CtaBanner title="Procurement your auditors will like" text="Every document says who created, approved, received, verified and paid it, and when." />
      </div>
    </div>
  );
}
