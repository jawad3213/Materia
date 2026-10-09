import PageMeta from "../../../shared/components/common/PageMeta";
import Hero from "../components/Hero";
import ProductPreview from "../components/ProductPreview";
import ProofStrip from "../components/ProofStrip";
import ProcurementCycle from "../components/ProcurementCycle";
import FeatureGrid from "../components/FeatureGrid";
import ThreeWayMatch from "../components/ThreeWayMatch";
import RoleShowcase from "../components/RoleShowcase";
import CtaBanner from "../components/CtaBanner";

/** The public home page: what Materia is, how the cycle flows and why it can be trusted. */
export default function HomePage() {
  return (
    <>
      <PageMeta
        title="Materia | Procurement from request to payment"
        description="Requisitions, purchase orders, receiving, returns, three-way matched invoices and payments in one controlled system."
      />
      <Hero />
      <ProductPreview />
      <ProofStrip />
      <ProcurementCycle />
      <FeatureGrid />
      <ThreeWayMatch />
      <RoleShowcase />
      <CtaBanner
        title="Bring every purchase under control"
        text="From the first request to the last payment, with the approvals, matching and audit trail your finance team expects."
      />
    </>
  );
}
