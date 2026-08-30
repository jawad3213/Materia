import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../../ui/table";
import Badge from "../../ui/badge/Badge";
import Checkbox from "../../form/input/Checkbox";
import { DownloadIcon, TrashBinIcon, PencilIcon } from "../../../icons";

const data = [
  {
    id: 1,
    name: "Lindsey Curtis",
    email: "demoemail@gmail.com",
    position: "Sales Assistant",
    salary: "$89,500",
    office: "Edinburgh",
    status: "Hired",
  },
  {
    id: 2,
    name: "Kaiya George",
    email: "demoemail@gmail.com",
    position: "Chief Executive Officer",
    salary: "$105,000",
    office: "London",
    status: "In Progress",
  },
  {
    id: 3,
    name: "Zain Geidt",
    email: "demoemail@gmail.com",
    position: "Junior Technical Author",
    salary: "$120,000",
    office: "San Francisco",
    status: "In Progress",
  },
  {
    id: 4,
    name: "Abram Schleifer",
    email: "demoemail@gmail.com",
    position: "Software Engineer",
    salary: "$95,000",
    office: "New York",
    status: "Hired",
  },
  {
    id: 5,
    name: "Carla George",
    email: "demoemail@gmail.com",
    position: "Integration Specialist",
    salary: "$80,000",
    office: "Chicago",
    status: "Pending",
  },
];

const getStatusStyle = (status: string) => {
  switch (status) {
    case "Hired":
      return "bg-success-50 text-success-600 dark:bg-success-500/10 dark:text-success-500";
    case "In Progress":
      return "bg-warning-50 text-warning-600 dark:bg-warning-500/10 dark:text-warning-500";
    case "Pending":
      return "bg-error-50 text-error-600 dark:bg-error-500/10 dark:text-error-500";
    default:
      return "bg-gray-100 text-gray-800 dark:bg-white/10 dark:text-white";
  }
};

export default function DataTableThree() {
  return (
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
      {/* Top Header */}
      <div className="flex flex-col gap-4 border-b border-gray-100 px-6 py-5 dark:border-white/[0.05] sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-2 text-sm font-medium text-gray-500 dark:text-gray-400">
          Show
          <select className="rounded-lg border border-gray-200 bg-transparent px-3 py-1.5 text-gray-700 outline-none focus:border-brand-500 dark:border-gray-800 dark:text-gray-300">
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="20">20</option>
          </select>
          entries
        </div>

        <div className="flex items-center gap-3">
          <div className="relative">
            <svg
              className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-gray-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
              />
            </svg>
            <input
              type="text"
              placeholder="Search..."
              className="w-full rounded-lg border border-gray-200 bg-transparent py-2 pl-9 pr-4 text-sm text-gray-700 outline-none focus:border-brand-500 dark:border-gray-800 dark:text-gray-300 sm:w-64"
            />
          </div>

          <button className="flex items-center gap-2 rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-400 dark:hover:bg-white/[0.03]">
            Download
            <DownloadIcon />
          </button>
        </div>
      </div>

      <div className="max-w-full overflow-x-auto">
        <Table>
          <TableHeader className="bg-transparent border-b border-gray-100 dark:border-white/[0.05]">
            <TableRow>
              <TableCell
                isHeader
                className="px-6 py-4 font-medium text-gray-900 text-start text-xs border border-gray-100 dark:border-white/[0.05] dark:text-white"
              >
                <Checkbox checked={false} onChange={() => {}} />
              </TableCell>
              {[
                { name: "User", sortable: true },
                { name: "Position", sortable: true },
                { name: "Salary", sortable: true },
                { name: "Office", sortable: true },
                { name: "Status", sortable: true },
                { name: "Action", sortable: true },
              ].map((header, i) => (
                <TableCell
                  key={i}
                  isHeader
                  className="px-6 py-4 font-medium text-gray-900 text-start text-xs border border-gray-100 dark:border-white/[0.05] dark:text-white"
                >
                  <div className="flex items-center justify-between gap-2">
                    {header.name}
                    {header.sortable && (
                      <button className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
                        <svg
                          className="size-3.5"
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M7 15l5 5 5-5M7 9l5-5 5 5"
                          />
                        </svg>
                      </button>
                    )}
                  </div>
                </TableCell>
              ))}
            </TableRow>
          </TableHeader>

          <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
            {data.map((row) => (
              <TableRow key={row.id}>
                <TableCell className="px-6 py-4 text-start border border-gray-100 dark:border-white/[0.05]">
                  <Checkbox checked={false} onChange={() => {}} />
                </TableCell>
                <TableCell className="px-6 py-4 text-start border border-gray-100 dark:border-white/[0.05]">
                  <div className="flex flex-col">
                    <span className="text-sm font-medium text-gray-800 dark:text-white/90">
                      {row.name}
                    </span>
                    <span className="text-sm text-gray-500 dark:text-gray-400">
                      {row.email}
                    </span>
                  </div>
                </TableCell>
                <TableCell className="px-6 py-4 text-start border border-gray-100 dark:border-white/[0.05]">
                  <span className="text-sm font-medium text-gray-500 dark:text-gray-400">
                    {row.position}
                  </span>
                </TableCell>
                <TableCell className="px-6 py-4 text-start border border-gray-100 dark:border-white/[0.05]">
                  <span className="text-sm font-medium text-gray-800 dark:text-white/90">
                    {row.salary}
                  </span>
                </TableCell>
                <TableCell className="px-6 py-4 text-start border border-gray-100 dark:border-white/[0.05]">
                  <span className="text-sm font-medium text-gray-500 dark:text-gray-400">
                    {row.office}
                  </span>
                </TableCell>
                <TableCell className="px-6 py-4 text-start border border-gray-100 dark:border-white/[0.05]">
                  <span
                    className={`inline-flex rounded-full px-2.5 py-1 text-xs font-medium ${getStatusStyle(
                      row.status
                    )}`}
                  >
                    {row.status}
                  </span>
                </TableCell>
                <TableCell className="px-6 py-4 text-start border border-gray-100 dark:border-white/[0.05]">
                  <div className="flex items-center gap-3">
                    <button className="text-gray-400 hover:text-red-500">
                      <TrashBinIcon className="size-5" />
                    </button>
                    <button className="text-gray-400 hover:text-brand-500">
                      <PencilIcon className="size-5" />
                    </button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {/* Bottom Pagination */}
      <div className="flex flex-col gap-4 border-t border-gray-100 px-6 py-5 dark:border-white/[0.05] sm:flex-row sm:items-center sm:justify-between">
        <p className="text-sm font-medium text-gray-500 dark:text-gray-400">
          Showing 1 to 5 of 10 entries
        </p>

        <div className="flex items-center gap-2">
          <button className="flex h-9 items-center justify-center rounded-lg border border-gray-200 px-4 text-sm font-medium text-gray-500 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-400 dark:hover:bg-white/[0.03]">
            Previous
          </button>
          <button className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand-500 text-white">
            1
          </button>
          <button className="flex h-9 w-9 items-center justify-center rounded-lg border border-gray-200 text-gray-500 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-400 dark:hover:bg-white/[0.03]">
            2
          </button>
          <button className="flex h-9 items-center justify-center rounded-lg border border-gray-200 px-4 text-sm font-medium text-gray-500 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-400 dark:hover:bg-white/[0.03]">
            Next
          </button>
        </div>
      </div>
    </div>
  );
}
