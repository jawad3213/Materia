import React from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../../ui/table";
import Badge from "../../ui/badge/Badge";

const campaigns = [
  {
    id: 1,
    userImage: "/images/user/user-01.jpg",
    userName: "Wilson Gouse",
    campaignTitle: "Grow your brand by...",
    campaignSubtitle: "Ads campaign",
    status: "Success",
    logo: (
      <svg className="size-6" viewBox="0 0 122.8 122.8">
        <path d="M25.8 77.6c0 7.1-5.8 12.9-12.9 12.9S0 84.7 0 77.6s5.8-12.9 12.9-12.9h12.9v12.9zm6.5 0c0-7.1 5.8-12.9 12.9-12.9s12.9 5.8 12.9 12.9v32.3c0 7.1-5.8 12.9-12.9 12.9s-12.9-5.8-12.9-12.9V77.6z" fill="#e01e5a"/>
        <path d="M45.2 25.8c-7.1 0-12.9-5.8-12.9-12.9S38.1 0 45.2 0s12.9 5.8 12.9 12.9v12.9H45.2zm0 6.5c7.1 0 12.9 5.8 12.9 12.9s-5.8 12.9-12.9 12.9H12.9C5.8 58.1 0 52.3 0 45.2s5.8-12.9 12.9-12.9h32.3z" fill="#36c5f0"/>
        <path d="M97 45.2c0-7.1 5.8-12.9 12.9-12.9s12.9 5.8 12.9 12.9-5.8 12.9-12.9 12.9H97V45.2zm-6.5 0c0 7.1-5.8 12.9-12.9 12.9s-12.9-5.8-12.9-12.9V12.9C64.7 5.8 70.5 0 77.6 0s12.9 5.8 12.9 12.9v32.3z" fill="#2eb67d"/>
        <path d="M77.6 97c7.1 0 12.9 5.8 12.9 12.9s-5.8 12.9-12.9 12.9-12.9-5.8-12.9-12.9V97h12.9zm0-6.5c-7.1 0-12.9-5.8-12.9-12.9s5.8-12.9 12.9-12.9h32.3c7.1 0 12.9 5.8 12.9 12.9s-5.8 12.9-12.9 12.9H77.6z" fill="#ecb22e"/>
      </svg>
    ),
  },
  {
    id: 2,
    userImage: "/images/user/user-02.jpg",
    userName: "Wilson Gouse",
    campaignTitle: "Make Better Ideas...",
    campaignSubtitle: "Ads campaign",
    status: "Pending",
    logo: (
      <svg className="size-6" viewBox="0 0 24 24" fill="none">
        <circle cx="12" cy="12" r="12" fill="#1877F2"/>
        <path d="M15.83 10.813L15.298 14.282H12.502V24h-3.92V14.282H6v-3.469h2.583V8.56c0-2.556 1.56-3.961 3.858-3.961 1.09 0 2.03.081 2.303.117v2.671l-1.58.001c-1.24 0-1.48.59-1.48 1.455v1.97h4.146z" fill="#fff"/>
      </svg>
    ),
  },
  {
    id: 3,
    userImage: "/images/user/user-03.jpg",
    userName: "Wilson Gouse",
    campaignTitle: "Increase your website tra...",
    campaignSubtitle: "Ads campaign",
    status: "Success",
    logo: (
      <svg className="size-6" viewBox="0 0 24 24">
        <path fill="#FABB05" d="M10.1 2.3l11.6 20.1-4 2.3L6.1 4.6z"/>
        <path fill="#4285F4" d="M21.7 4.6L10.1 24.7l-4-2.3L17.7 2.3z"/>
        <path fill="#34A853" d="M2.1 16.1l4 6.9 4-2.3-4-6.9z"/>
      </svg>
    ),
  },
  {
    id: 4,
    userImage: "/images/user/user-04.jpg",
    userName: "Wilson Gouse",
    campaignTitle: "Grow your brand by...",
    campaignSubtitle: "Ads campaign",
    status: "Failed",
    logo: (
      <svg className="size-6" viewBox="0 0 24 24" fill="none">
        <defs>
          <linearGradient id="ig-grad" x1="2" y1="22" x2="22" y2="2">
            <stop offset="0%" stopColor="#f09433" />
            <stop offset="25%" stopColor="#e6683c" />
            <stop offset="50%" stopColor="#dc2743" />
            <stop offset="75%" stopColor="#cc2366" />
            <stop offset="100%" stopColor="#bc1888" />
          </linearGradient>
        </defs>
        <rect x="2" y="2" width="20" height="20" rx="5" fill="url(#ig-grad)" />
        <circle cx="12" cy="12" r="5" stroke="#fff" strokeWidth="2" />
        <circle cx="17.5" cy="6.5" r="1.5" fill="#fff" />
      </svg>
    ),
  },
  {
    id: 5,
    userImage: "/images/user/user-05.jpg",
    userName: "Wilson Gouse",
    campaignTitle: "Grow your brand by...",
    campaignSubtitle: "Ads campaign",
    status: "Success",
    logo: (
      <svg className="size-6" viewBox="0 0 24 24">
        <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
        <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
        <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
        <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
      </svg>
    ),
  },
  {
    id: 6,
    userImage: "/images/user/user-06.jpg",
    userName: "Wilson Gouse",
    campaignTitle: "Grow your brand by...",
    campaignSubtitle: "Ads campaign",
    status: "Success",
    logo: (
      <svg className="size-6" viewBox="0 0 24 24">
        <path fill="#FF0000" d="M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814z"/>
        <path fill="#fff" d="M9.545 15.568V8.432L15.818 12l-6.273 3.568z"/>
      </svg>
    ),
  },
];

export default function BasicTableFour() {
  return (
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
      <div className="flex items-center justify-between px-6 py-6">
        <h3 className="text-[18px] font-semibold text-gray-800 dark:text-white/90">
          Featured Campaigns
        </h3>
        <button className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
          <svg className="size-5" fill="currentColor" viewBox="0 0 24 24">
            <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"/>
          </svg>
        </button>
      </div>
      
      <div className="max-w-full overflow-x-auto">
        <Table>
          <TableHeader className="bg-transparent border-y border-gray-100 dark:border-white/[0.05]">
            <TableRow>
              <TableCell
                isHeader
                className="px-6 py-4 font-medium text-gray-500 text-start text-xs dark:text-gray-400"
              >
                Products
              </TableCell>
              <TableCell
                isHeader
                className="px-6 py-4 font-medium text-gray-500 text-start text-xs dark:text-gray-400"
              >
                Campaign
              </TableCell>
              <TableCell
                isHeader
                className="px-6 py-4 font-medium text-gray-500 text-start text-xs dark:text-gray-400"
              >
                Status
              </TableCell>
            </TableRow>
          </TableHeader>
          
          <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
            {campaigns.map((campaign) => (
              <TableRow key={campaign.id}>
                <TableCell className="px-6 py-4 text-start">
                  <div className="flex items-center gap-4">
                    <img
                      src={campaign.userImage}
                      alt={campaign.userName}
                      className="h-9 w-9 rounded-full object-cover"
                    />
                    <span className="font-medium text-gray-700 text-sm dark:text-white/90">
                      {campaign.userName}
                    </span>
                  </div>
                </TableCell>
                
                <TableCell className="px-6 py-4 text-start">
                  <div className="flex items-center gap-4">
                    {campaign.logo}
                    <div className="flex flex-col">
                      <span className="font-semibold text-gray-800 text-sm dark:text-white/90">
                        {campaign.campaignTitle}
                      </span>
                      <span className="text-xs text-gray-500 dark:text-gray-400">
                        {campaign.campaignSubtitle}
                      </span>
                    </div>
                  </div>
                </TableCell>
                
                <TableCell className="px-6 py-4 text-start">
                  <Badge
                    size="sm"
                    variant="light"
                    color={
                      campaign.status === "Success"
                        ? "success"
                        : campaign.status === "Pending"
                        ? "warning"
                        : "error"
                    }
                  >
                    {campaign.status}
                  </Badge>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
