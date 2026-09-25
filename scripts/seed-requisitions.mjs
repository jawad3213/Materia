/**
 * Materia Purchase Requisitions Seeder
 *
 * Seeds realistic, production-grade Purchase Requisitions into Materia:
 * - Authenticates with admin credentials (JWT Bearer)
 * - Dynamically binds lines to real materials & suppliers in the database
 * - Creates requisitions across all lifecycles:
 *   * DRAFT (Work-in-progress by engineers / department heads)
 *   * SUBMITTED (Pending review in the Approvals Portal)
 *   * APPROVED (Approved by management, ready for PO conversion)
 *   * REJECTED (Denied with transparent audit reason)
 *   * CANCELLED (Revoked requests)
 *
 * Usage: node scripts/seed-requisitions.mjs
 */

const ROOT_API_URL = process.env.ROOT_API_URL || 'http://localhost:8080/api/v1';
const AUTH_URL = `${ROOT_API_URL}/auth/login`;
const MATERIALS_URL = `${ROOT_API_URL}/masterdata/materials`;
const REQUISITIONS_URL = `${ROOT_API_URL}/purchase-requisitions`;

const ADMIN_CREDENTIALS = {
  email: process.env.ADMIN_EMAIL || 'admin@materia.com',
  password: process.env.ADMIN_PASSWORD || 'admin123',
};

async function fetchJson(url, options = {}) {
  const res = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...(options.headers || {}),
    },
  });

  const text = await res.text();
  let data;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text;
  }

  if (!res.ok) {
    const errorMsg = data?.message || data?.error || (typeof data === 'string' ? data : `HTTP ${res.status}`);
    const err = new Error(errorMsg);
    err.status = res.status;
    err.body = data;
    throw err;
  }

  return data;
}

async function login() {
  console.log(`🔑 Authenticating as ${ADMIN_CREDENTIALS.email}...`);
  try {
    const data = await fetchJson(AUTH_URL, {
      method: 'POST',
      body: JSON.stringify(ADMIN_CREDENTIALS),
    });
    if (!data.accessToken) {
      throw new Error('No access token in response');
    }
    console.log(`✅ Authentication successful (User: ${data.user?.name || data.user?.email})`);
    return data.accessToken;
  } catch (err) {
    console.error(`❌ Authentication failed:`, err.message);
    throw err;
  }
}

async function getMaterials() {
  console.log(`📦 Fetching materials from inventory database...`);
  try {
    const res = await fetchJson(MATERIALS_URL);
    const materials = Array.isArray(res) ? res : (res?.content || []);
    console.log(`✅ Loaded ${materials.length} available materials from catalog.`);
    return materials;
  } catch (err) {
    console.warn(`⚠️ Could not fetch materials: ${err.message}. Using fallback line items.`);
    return [];
  }
}

async function seed() {
  console.log('====================================================');
  console.log('🌱 Materia Purchase Requisitions Seeder');
  console.log(`🎯 Target API Base: ${ROOT_API_URL}`);
  console.log('====================================================\n');

  const token = await login();
  const authHeaders = { Authorization: `Bearer ${token}` };

  const materials = await getMaterials();
  const getMat = (idx) => materials[idx % materials.length] || null;

  const REQUISITION_DEFINITIONS = [
    // 1. DRAFT Requisitions
    {
      title: 'Q1 Structural Metal & Fasteners Replenishment',
      description: 'Periodic replenishment of raw metal materials and structural sheets for industrial production line',
      justification: 'Safety stock replenishment for upcoming production schedule 2026',
      requesterId: 'usr-eng-001',
      requesterName: 'Karim Bennani',
      requiredDate: '2026-11-30',
      currencyCode: 'MAD',
      targetStatus: 'DRAFT',
      linesCount: 2,
    },
    {
      title: 'Logistics Packaging & Heavy-Duty Carton Stock',
      description: 'Industrial corrugated shipping boxes, strapping tape, and pallet stretch film',
      justification: 'Warehouse export shipping buffer replenishment',
      requesterId: 'usr-log-003',
      requesterName: 'Tariq Mansoor',
      requiredDate: '2026-12-05',
      currencyCode: 'MAD',
      targetStatus: 'DRAFT',
      linesCount: 1,
    },
    {
      title: 'High-Grade Synthetic Lubricants & Industrial Cleaners',
      description: 'CNC machine coolants and hydraulic oils for plant maintenance schedule',
      justification: 'Biannual scheduled facility machine servicing',
      requesterId: 'usr-maint-004',
      requesterName: 'Yassine Chraibi',
      requiredDate: '2026-12-10',
      currencyCode: 'MAD',
      targetStatus: 'DRAFT',
      linesCount: 2,
    },

    // 2. SUBMITTED Requisitions (Waiting in Approvals Portal)
    {
      title: 'Microcontrollers & Passive Components for IoT Batch',
      description: 'SMD resistors, ceramic capacitors, and ARM Cortex modules for controller production',
      justification: 'Contract delivery for Smart City automated sensor deployment',
      requesterId: 'usr-rd-002',
      requesterName: 'Sanaa Idrissi',
      requiredDate: '2026-11-15',
      currencyCode: 'MAD',
      targetStatus: 'SUBMITTED',
      linesCount: 3,
    },
    {
      title: 'Workshop Precision Measuring Instruments & Calipers',
      description: 'Digital micrometer set, dial indicators, and granite surface plates',
      justification: 'Quality assurance laboratory equipment upgrade',
      requesterId: 'usr-qa-005',
      requesterName: 'Nadia El Fassi',
      requiredDate: '2026-11-25',
      currencyCode: 'MAD',
      targetStatus: 'SUBMITTED',
      linesCount: 2,
    },
    {
      title: 'High-Temperature Industrial Thermal Insulators',
      description: 'Ceramic fiber blankets and refractory sealing gaskets for heat-treatment furnaces',
      justification: 'Preventive overhaul on furnace 2 chamber lining',
      requesterId: 'usr-plant-006',
      requesterName: 'Omar Benjelloun',
      requiredDate: '2026-12-01',
      currencyCode: 'MAD',
      targetStatus: 'SUBMITTED',
      linesCount: 1,
    },

    // 3. APPROVED Requisitions (Ready to Convert to PO)
    {
      title: 'Specialty Alloy Rods & CNC Tooling Inserts',
      description: 'Carbide milling inserts, titanium-coated drill bits, and brass stock bars',
      justification: 'Urgent replacement tooling for export turbine component manufacturing',
      requesterId: 'usr-prod-007',
      requesterName: 'Rachid Tazi',
      requiredDate: '2026-11-10',
      currencyCode: 'MAD',
      targetStatus: 'APPROVED',
      approvalNotes: 'Validated by Engineering & Plant Director. Approved within budget.',
      linesCount: 2,
    },
    {
      title: 'Automated Pneumatic Valves & Pressure Regulators',
      description: 'Festo compatible pneumatic cylinders, solenoid valves, and polyurethane tubing',
      justification: 'Production line 4 automated packaging station retrofit',
      requesterId: 'usr-auto-008',
      requesterName: 'Fatima Zahra Alaoui',
      requiredDate: '2026-11-20',
      currencyCode: 'MAD',
      targetStatus: 'APPROVED',
      approvalNotes: 'Capital expenditure approved by VP Operations.',
      linesCount: 2,
    },

    // 4. REJECTED Requisitions
    {
      title: 'Unbudgeted Premium Ergonomic Office Accessories',
      description: 'Dual monitor arm mounts, motorized desks, and mechanical keyboards',
      justification: 'Department workspace upgrade request',
      requesterId: 'usr-admin-009',
      requesterName: 'Amine Kabbaj',
      requiredDate: '2026-11-05',
      currencyCode: 'MAD',
      targetStatus: 'REJECTED',
      rejectionReason: 'Non-critical equipment. Please resubmit under next fiscal year office facilities budget.',
      linesCount: 1,
    },

    // 5. CANCELLED Requisitions
    {
      title: 'Redundant Extruded Aluminum Profiles Order',
      description: 'T-slot 40x40 aluminum modular framing profiles and corner brackets',
      justification: 'Framework assembly for test rigs',
      requesterId: 'usr-eng-001',
      requesterName: 'Karim Bennani',
      requiredDate: '2026-11-12',
      currencyCode: 'MAD',
      targetStatus: 'CANCELLED',
      cancellationReason: 'Cancelled by requester: materials already available in surplus rack WH-02.',
      linesCount: 1,
    },
  ];

  console.log(`\n📋 Seeding ${REQUISITION_DEFINITIONS.length} Purchase Requisitions...\n`);

  let createdCount = 0;
  let lineCounter = 0;

  for (const def of REQUISITION_DEFINITIONS) {
    const lines = [];
    for (let i = 0; i < def.linesCount; i++) {
      const mat = getMat(lineCounter++);
      lines.push({
        materialId: mat?.id || null,
        materialCode: mat?.code || `MAT-${String(i + 1).padStart(4, '0')}`,
        materialName: mat?.name || `Material Item ${i + 1}`,
        supplierId: mat?.supplierId || null,
        supplierCode: mat?.supplierCode || null,
        quantity: Math.floor(Math.random() * 80) + 20,
        requiredDate: def.requiredDate,
        notes: `Delivery term: DAP Casablanca. Priority item #${i + 1}`,
        deliveryTerms: 'DAP Casablanca Warehouse',
        storageLocation: `WH-MAIN-A${(i % 5) + 1}`,
      });
    }

    const payload = {
      title: def.title,
      description: def.description,
      justification: def.justification,
      requesterId: def.requesterId,
      requesterName: def.requesterName,
      requiredDate: def.requiredDate,
      currencyCode: def.currencyCode,
      createdBy: 'SEED_SCRIPT',
      lines,
    };

    try {
      // 1. Create requisition (starts in DRAFT)
      const created = await fetchJson(REQUISITIONS_URL, {
        method: 'POST',
        headers: authHeaders,
        body: JSON.stringify(payload),
      });

      const reqId = created.id;
      const reqCode = created.requisitionCode || created.code;

      // 2. Lifecycle transitions based on targetStatus
      if (def.targetStatus === 'SUBMITTED') {
        await fetchJson(`${REQUISITIONS_URL}/${reqId}/submit?userId=${encodeURIComponent(def.requesterId)}`, {
          method: 'PATCH',
          headers: authHeaders,
        });
      } else if (def.targetStatus === 'APPROVED') {
        await fetchJson(`${REQUISITIONS_URL}/${reqId}/submit?userId=${encodeURIComponent(def.requesterId)}`, {
          method: 'PATCH',
          headers: authHeaders,
        });
        const notes = encodeURIComponent(def.approvalNotes || 'Approved');
        await fetchJson(
          `${REQUISITIONS_URL}/${reqId}/approve?approverId=usr-admin-01&approverName=Jaouad+El+Hail&notes=${notes}`,
          { method: 'PATCH', headers: authHeaders }
        );
      } else if (def.targetStatus === 'REJECTED') {
        await fetchJson(`${REQUISITIONS_URL}/${reqId}/submit?userId=${encodeURIComponent(def.requesterId)}`, {
          method: 'PATCH',
          headers: authHeaders,
        });
        const reason = encodeURIComponent(def.rejectionReason || 'Rejected by procurement reviewer');
        await fetchJson(
          `${REQUISITIONS_URL}/${reqId}/reject?approverId=usr-admin-01&approverName=Jaouad+El+Hail&reason=${reason}`,
          { method: 'PATCH', headers: authHeaders }
        );
      } else if (def.targetStatus === 'CANCELLED') {
        await fetchJson(`${REQUISITIONS_URL}/${reqId}/submit?userId=${encodeURIComponent(def.requesterId)}`, {
          method: 'PATCH',
          headers: authHeaders,
        });
        const reason = encodeURIComponent(def.cancellationReason || 'Cancelled');
        await fetchJson(
          `${REQUISITIONS_URL}/${reqId}/cancel?userId=${encodeURIComponent(def.requesterId)}&reason=${reason}`,
          { method: 'PATCH', headers: authHeaders }
        );
      }

      console.log(`  ✅ [${def.targetStatus.padEnd(9)}] ${reqCode} - "${def.title}" (${lines.length} lines)`);
      createdCount++;
    } catch (err) {
      console.error(`  ❌ [FAILED] "${def.title}":`, err.message);
    }
  }

  console.log(`\n====================================================`);
  console.log(`🎉 REQUISITION SEEDING COMPLETE: ${createdCount}/${REQUISITION_DEFINITIONS.length} Requisitions Created!`);
  console.log('====================================================\n');
}

seed().catch((err) => {
  console.error('\n❌ Unhandled error in seed-requisitions:', err);
  process.exit(1);
});
