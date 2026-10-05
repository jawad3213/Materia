import { useEffect, useRef, useState } from "react";
import { Link, NavLink, Outlet, useLocation } from "react-router-dom";
import Button from "../../../shared/components/ui/button/Button";
import { ThemeToggleButton } from "../../../shared/components/common/ThemeToggleButton";
import useAuth from "../../auth/hooks/useAuth";
import { gsap, MOTION_OK, ScrollTrigger, useGSAP } from "../animations/gsapSetup";
import { ARROW_RIGHT } from "../components/landingIcons";

const NAV = [
  { to: "/", label: "Overview" },
  { to: "/features", label: "Features" },
  { to: "/security", label: "Security" },
];

function Logo() {
  return (
    <Link to="/" className="flex items-center gap-2.5" aria-label="Materia home">
      <img
        src="/images/Black_White_Minimalist_Professional_Initial_Logo__1_-removebg-preview332.png"
        alt=""
        className="h-10 w-10 object-contain dark:invert"
      />
      <img src="/images/Black_White_Minimalist_Professional_Initial_Logo__2_-removebg-preview.png" alt="Materia" className="h-6 w-auto object-contain dark:invert" />
    </Link>
  );
}

/** The public site: sticky header that condenses on scroll, page content, footer. */
export default function PublicLayout() {
  const { isAuthenticated } = useAuth();
  const { pathname } = useLocation();
  const headerRef = useRef<HTMLElement>(null);
  const [menuOpen, setMenuOpen] = useState(false);

  // Each page starts at the top, and its scroll animations are measured afresh.
  useEffect(() => {
    window.scrollTo(0, 0);
    ScrollTrigger.refresh();
  }, [pathname]);

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

  return (
    <div className="min-h-screen bg-white font-outfit text-gray-800 dark:bg-gray-950 dark:text-white/90">
      <header
        ref={headerRef}
        className="group/header fixed inset-x-0 top-0 z-50 transition-all duration-300 [&.is-condensed]:border-b [&.is-condensed]:border-gray-200/70 [&.is-condensed]:bg-white/80 [&.is-condensed]:backdrop-blur-xl dark:[&.is-condensed]:border-white/10 dark:[&.is-condensed]:bg-gray-950/80"
      >
        <div className="mx-auto flex h-20 max-w-7xl items-center justify-between px-5 transition-all duration-300 group-[.is-condensed]/header:h-16 sm:px-8">
          <Logo />

          <nav className="hidden items-center gap-1 md:flex" aria-label="Main">
            {NAV.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end
                className={({ isActive }) =>
                  `rounded-full px-4 py-2 text-sm font-medium transition-colors ${
                    isActive ? "bg-brand-50 text-brand-600 dark:bg-brand-500/15 dark:text-brand-300" : "text-gray-600 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white"
                  }`
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>

          <div className="flex items-center gap-3">
            <ThemeToggleButton />
            <Link to={isAuthenticated ? "/dashboard" : "/login"} className="hidden sm:block">
              <Button size="sm" endIcon={ARROW_RIGHT}>
                {isAuthenticated ? "Open Dashboard" : "Sign In"}
              </Button>
            </Link>
            <button
              type="button"
              onClick={() => setMenuOpen((open) => !open)}
              className="flex size-10 items-center justify-center rounded-full border border-gray-200 text-gray-600 md:hidden dark:border-gray-800 dark:text-gray-300"
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
          <div className="border-t border-gray-200 bg-white px-5 py-4 md:hidden dark:border-gray-800 dark:bg-gray-950">
            {NAV.map((item) => (
              <Link key={item.to} to={item.to} onClick={() => setMenuOpen(false)} className="block rounded-lg px-3 py-3 text-base font-medium text-gray-700 dark:text-gray-200">
                {item.label}
              </Link>
            ))}
            <Link to={isAuthenticated ? "/dashboard" : "/login"} onClick={() => setMenuOpen(false)} className="mt-2 block">
              <Button className="w-full">{isAuthenticated ? "Open Dashboard" : "Sign In"}</Button>
            </Link>
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
              {NAV.map((item) => (
                <li key={item.to}>
                  <Link to={item.to} className="hover:text-brand-500">
                    {item.label}
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
