import { useEffect, useRef, useState } from "react";
import { Link, NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import Button from "../../../shared/components/ui/button/Button";
import useAuth from "../../auth/hooks/useAuth";
import { gsap, MOTION_OK, ScrollTrigger, useGSAP } from "../animations/gsapSetup";
import { ARROW_RIGHT } from "../components/landingIcons";

/** Sections of the home page, for footer references. */
const SECTIONS = [
  { id: "overview", label: "Overview" },
  { id: "cycle", label: "How it works" },
  { id: "features", label: "Features" },
  { id: "matching", label: "Three-way match" },
  { id: "roles", label: "Roles" },
];

/** The public pages in the main navigation. */
const PAGES = [
  { to: "/", label: "Overview", end: true },
  { to: "/features", label: "Modules", end: false },
  { to: "/security", label: "Security", end: false },
];

const HEADER_OFFSET = 72;

function Logo() {
  return (
    <Link to="/" className="flex shrink-0 items-center gap-2.5" aria-label="Materia home">
      <img
        src="/images/Black_White_Minimalist_Professional_Initial_Logo__1_-removebg-preview332.png"
        alt=""
        className="h-10 w-10 object-contain dark:invert"
      />
      <img
        src="/images/Black_White_Minimalist_Professional_Initial_Logo__2_-removebg-preview.png"
        alt="Materia"
        className="h-6 w-auto object-contain dark:invert"
      />
    </Link>
  );
}

const linkClass = (active: boolean) =>
  `whitespace-nowrap rounded-full px-4 py-2 text-sm font-medium transition-colors ${
    active ? "bg-brand-50 text-brand-600 dark:bg-brand-500/15 dark:text-brand-300" : "text-gray-600 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white"
  }`;

/** Scrolls smoothly to a home-page section, below the fixed header; instantly when motion is reduced. */
function scrollToSection(id: string) {
  const target = document.getElementById(id);
  if (!target) return;
  const smooth = window.matchMedia?.(MOTION_OK).matches ?? false;
  gsap.to(window, { scrollTo: { y: target, offsetY: id === "overview" ? 0 : HEADER_OFFSET }, duration: smooth ? 1.1 : 0, ease: "power3.inOut" });
}

/** The public site: sticky header with navigation, page content, footer. */
export default function PublicLayout() {
  const { isAuthenticated } = useAuth();
  const { pathname, hash } = useLocation();
  const navigate = useNavigate();
  const headerRef = useRef<HTMLElement>(null);
  const [menuOpen, setMenuOpen] = useState(false);
  const onHome = pathname === "/";

  // A page opens at the top, or at the section named in the link (/#features); animations are measured afresh.
  useEffect(() => {
    ScrollTrigger.refresh();
    const section = hash.replace("#", "");
    if (pathname === "/" && section) {
      const timer = window.setTimeout(() => scrollToSection(section), 60);
      return () => window.clearTimeout(timer);
    }
    window.scrollTo(0, 0);
  }, [pathname, hash]);

  useGSAP(
    () => {
      const mm = gsap.matchMedia();
      mm.add(MOTION_OK, () => {
        gsap.from(headerRef.current, { yPercent: -100, opacity: 0, duration: 0.8, ease: "power3.out" });
      });
      ScrollTrigger.create({
        start: 40,
        end: "max",
        onToggle: (self) => headerRef.current?.classList.toggle("is-condensed", self.isActive),
      });
    },
    { scope: headerRef }
  );

  const goToSection = (id: string) => {
    setMenuOpen(false);
    if (onHome) {
      scrollToSection(id);
      window.history.replaceState(null, "", id === "overview" ? "/" : `/#${id}`);
    } else {
      navigate(id === "overview" ? "/" : `/#${id}`);
    }
  };

  return (
    <div className="min-h-screen bg-white font-outfit text-gray-800 dark:bg-gray-950 dark:text-white/90">
      <header
        ref={headerRef}
        className="group/header fixed inset-x-0 top-0 z-50 transition-all duration-300 [&.is-condensed]:border-b [&.is-condensed]:border-gray-200/70 [&.is-condensed]:bg-white/80 [&.is-condensed]:backdrop-blur-xl dark:[&.is-condensed]:border-white/10 dark:[&.is-condensed]:bg-gray-950/80"
      >
        <div className="mx-auto flex h-20 max-w-7xl items-center justify-between gap-4 px-5 transition-all duration-300 group-[.is-condensed]/header:h-16 sm:px-8">
          <Logo />

          <nav className="hidden items-center gap-1.5 sm:flex" aria-label="Main">
            {PAGES.map((page) => (
              <NavLink
                key={page.to}
                to={page.to}
                end={page.end}
                className={({ isActive }) => linkClass(isActive)}
              >
                {page.label}
              </NavLink>
            ))}
          </nav>

          <div className="flex shrink-0 items-center gap-3">
            {isAuthenticated ? (
              <Link to="/dashboard">
                <Button size="sm" endIcon={ARROW_RIGHT}>
                  Open Dashboard
                </Button>
              </Link>
            ) : (
              <Link to="/login">
                <Button size="sm">
                  Sign in
                </Button>
              </Link>
            )}
            <button
              type="button"
              onClick={() => setMenuOpen((open) => !open)}
              className="flex size-10 items-center justify-center rounded-full border border-gray-200 text-gray-600 sm:hidden dark:border-gray-800 dark:text-gray-300"
              aria-label="Toggle menu"
              aria-expanded={menuOpen}
            >
              <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={menuOpen ? "M6 18L18 6M6 6l12 12" : "M4 7h16M4 12h16M4 17h16"} />
              </svg>
            </button>
          </div>
        </div>

        {menuOpen && (
          <div className="max-h-[calc(100vh-4rem)] overflow-y-auto border-t border-gray-200 bg-white px-5 py-4 sm:hidden dark:border-gray-800 dark:bg-gray-950">
            <p className="px-3 pb-1 text-xs font-semibold uppercase tracking-widest text-gray-400">Navigation</p>
            {PAGES.map((page) => (
              <Link key={page.to} to={page.to} onClick={() => setMenuOpen(false)} className="block rounded-lg px-3 py-2.5 text-base font-medium text-gray-700 dark:text-gray-200">
                {page.label}
              </Link>
            ))}
            <div className="mt-4 border-t border-gray-100 pt-4 dark:border-gray-800">
              {isAuthenticated ? (
                <Link to="/dashboard" onClick={() => setMenuOpen(false)}>
                  <Button className="w-full">Open Dashboard</Button>
                </Link>
              ) : (
                <Link to="/login" onClick={() => setMenuOpen(false)}>
                  <Button className="w-full">Sign in</Button>
                </Link>
              )}
            </div>
          </div>
        )}
      </header>

      <main>
        <Outlet />
      </main>

      <footer className="border-t border-gray-200 bg-gray-50 dark:border-white/10 dark:bg-gray-950">
        <div className="mx-auto grid max-w-7xl gap-10 px-5 py-14 sm:px-8 md:grid-cols-4">
          <div className="md:col-span-2">
            <Logo />
            <p className="mt-4 max-w-sm text-sm leading-relaxed text-gray-500 dark:text-gray-400">
              Materia runs your whole procurement cycle, from the first request to the last payment, with the controls your finance team expects.
            </p>
          </div>
          <div>
            <p className="text-sm font-semibold text-gray-900 dark:text-white">Product</p>
            <ul className="mt-4 space-y-3 text-sm text-gray-500 dark:text-gray-400">
              {SECTIONS.slice(1).map((section) => (
                <li key={section.id}>
                  <button type="button" onClick={() => goToSection(section.id)} className="hover:text-brand-500">
                    {section.label}
                  </button>
                </li>
              ))}
              {PAGES.map((page) => (
                <li key={page.to}>
                  <Link to={page.to} className="hover:text-brand-500">
                    {page.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
          <div>
            <p className="text-sm font-semibold text-gray-900 dark:text-white">Account</p>
            <ul className="mt-4 space-y-3 text-sm text-gray-500 dark:text-gray-400">
              {isAuthenticated ? (
                <li>
                  <Link to="/dashboard" className="hover:text-brand-500">
                    Open dashboard
                  </Link>
                </li>
              ) : (
                <>
                  <li>
                    <Link to="/login" className="hover:text-brand-500">
                      Sign in
                    </Link>
                  </li>
                  <li>
                    <Link to="/forgot-password" className="hover:text-brand-500">
                      Reset password
                    </Link>
                  </li>
                </>
              )}
            </ul>
          </div>
        </div>
        <div className="border-t border-gray-200 py-6 text-center text-xs text-gray-400 dark:border-white/10">
          © {new Date().getFullYear()} Materia. Procurement, end to end.
        </div>
      </footer>
    </div>
  );
}
