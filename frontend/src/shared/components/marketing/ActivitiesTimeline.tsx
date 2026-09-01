import React from "react";

const activities = [
  {
    name: "Francisco Grbbs",
    action: "created invoice",
    target: "PQ-4491C",
    time: "Just Now",
    image: "/images/user/user-17.jpg",
    statusIcon: true,
  },
  {
    name: "Courtney Henry",
    action: "created invoice",
    target: "HK-234G",
    time: "15 minutes ago",
    image: "/images/user/user-18.jpg",
  },
  {
    name: "Bessie Cooper",
    action: "created invoice",
    target: "LH-2891C",
    time: "5 months ago",
    image: "/images/user/user-19.jpg",
  },
  {
    name: "Theresa Web",
    action: "created invoice",
    target: "CK-125NH",
    time: "2 weeks ago",
    image: "/images/user/user-20.jpg",
  },
];

export default function ActivitiesTimeline() {
  return (
    <div className="p-6 bg-white border border-gray-200 rounded-2xl dark:bg-white/[0.03] dark:border-gray-800">
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
          Activities
        </h3>
        <button className="text-gray-400 hover:text-gray-800 dark:hover:text-white">
          <svg
            width="20"
            height="20"
            viewBox="0 0 20 20"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M10.0003 10.8333C10.4606 10.8333 10.8337 10.4602 10.8337 10C10.8337 9.53975 10.4606 9.16666 10.0003 9.16666C9.54009 9.16666 9.16699 9.53975 9.16699 10C9.16699 10.4602 9.54009 10.8333 10.0003 10.8333Z"
              stroke="currentColor"
              strokeWidth="1.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
            <path
              d="M10.0003 4.99999C10.4606 4.99999 10.8337 4.62689 10.8337 4.16666C10.8337 3.70642 10.4606 3.33333 10.0003 3.33333C9.54009 3.33333 9.16699 3.70642 9.16699 4.16666C9.16699 4.62689 9.54009 4.99999 10.0003 4.99999Z"
              stroke="currentColor"
              strokeWidth="1.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
            <path
              d="M10.0003 16.6667C10.4606 16.6667 10.8337 16.2936 10.8337 15.8333C10.8337 15.3731 10.4606 15 10.0003 15C9.54009 15 9.16699 15.3731 9.16699 15.8333C9.16699 16.2936 9.54009 16.6667 10.0003 16.6667Z"
              stroke="currentColor"
              strokeWidth="1.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </button>
      </div>
      <div className="relative pl-4 border-l border-gray-100 dark:border-gray-800 ml-4 space-y-6">
        {activities.map((activity, index) => (
          <div key={index} className="relative pl-6">
            <div className="absolute -left-10 top-0 w-8 h-8 rounded-full border-2 border-white dark:border-gray-900 overflow-hidden bg-gray-100">
              <img
                src={activity.image}
                alt={activity.name}
                className="w-full h-full object-cover"
                onError={(e) => {
                  (e.target as HTMLImageElement).src =
                    "https://ui-avatars.com/api/?name=" +
                    activity.name.replace(" ", "+");
                }}
              />
            </div>
            {activity.statusIcon && (
              <div className="flex items-center gap-1.5 mb-1">
                <span className="text-success-500">
                  <svg
                    width="14"
                    height="14"
                    viewBox="0 0 14 14"
                    fill="none"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path
                      d="M2.33301 7.58333H9.33301C9.65517 7.58333 9.91634 7.32217 9.91634 7V3.5C9.91634 3.17783 9.65517 2.91667 9.33301 2.91667H2.33301V7.58333ZM1.16634 1.16667V12.8333M2.33301 8.75V2.91667C2.33301 2.5945 2.59418 2.33333 2.91634 2.33333H9.33301C10.3 2.33333 11.083 3.11633 11.083 4.08333V6.41667C11.083 7.38367 10.3 8.16667 9.33301 8.16667H2.91634C2.59418 8.16667 2.33301 7.9055 2.33301 7.58333V8.75Z"
                      stroke="currentColor"
                      strokeWidth="1.2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    />
                  </svg>
                </span>
                <span className="text-xs font-medium text-success-500">
                  New invoice
                </span>
              </div>
            )}
            <p className="text-sm text-gray-500 dark:text-gray-400">
              <span className="font-semibold text-gray-800 dark:text-white">
                {activity.name}
              </span>{" "}
              {activity.action}{" "}
              <span className="font-medium text-gray-800 dark:text-white">
                {activity.target}
              </span>
            </p>
            <span className="block mt-1 text-xs text-gray-400">
              {activity.time}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
