import React from "react";

export default function PaymentMethods() {
  return (
    <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
      <div className="flex flex-col gap-4 border-b border-gray-200 px-6 py-5 sm:flex-row sm:items-center sm:justify-between dark:border-gray-800">
        <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
          Payment Methods
        </h3>
        <button className="flex items-center justify-center gap-2 rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-300 dark:hover:bg-gray-800 transition-colors">
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          Add New Card
        </button>
      </div>

      <div className="p-6">
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
          {/* Mastercard */}
          <div className="flex flex-col justify-between rounded-xl border border-gray-200 p-5 dark:border-gray-800">
            <div className="flex gap-4">
              <div className="flex h-12 w-12 items-center justify-center rounded-lg border border-gray-100 bg-white dark:border-gray-800 dark:bg-gray-900 shadow-sm shrink-0">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="7" cy="12" r="7" fill="#EB001B"/>
                  <circle cx="17" cy="12" r="7" fill="#F79E1B"/>
                </svg>
              </div>
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <h4 className="text-base font-medium text-gray-800 dark:text-white/90">Mastercard</h4>
                  <span className="flex items-center gap-1 rounded-full bg-success-50 px-2 py-0.5 text-xs font-medium text-success-600 dark:bg-success-500/10 dark:text-success-500">
                    <svg className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    Default
                  </span>
                </div>
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  **** **** **** 9029 &nbsp;&nbsp;&nbsp; Expiry 01/24
                </p>
              </div>
            </div>
            <div className="mt-6 flex justify-end gap-2">
              <button className="rounded-md border border-gray-200 px-4 py-1.5 text-xs font-medium text-gray-600 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-300 dark:hover:bg-gray-800">
                Edit
              </button>
              <button className="rounded-md border border-gray-200 px-4 py-1.5 text-xs font-medium text-gray-600 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-300 dark:hover:bg-gray-800">
                Delete
              </button>
            </div>
          </div>

          {/* Visa */}
          <div className="flex flex-col justify-between rounded-xl border border-gray-200 p-5 dark:border-gray-800">
            <div className="flex gap-4">
              <div className="flex h-12 w-12 items-center justify-center rounded-lg border border-gray-100 bg-white dark:border-gray-800 dark:bg-gray-900 shadow-sm shrink-0">
                <svg width="32" height="10" viewBox="0 0 32 10" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12.9806 0.170898L8.47167 9.8517H5.48559L3.48622 2.22818C3.39345 1.83407 3.25433 1.63708 2.92985 1.45558C2.23439 1.0471 0.997782 0.638608 0 0.441618L0.09277 0.170898H4.72828C5.33128 0.170898 5.85698 0.594611 6.01162 1.35122L7.20235 6.64757L10.011 0.170898H12.9806ZM23.1507 6.78393C23.1661 4.22659 19.6421 4.07526 19.6576 2.87979C19.6731 2.50148 20.0442 2.07777 20.9102 1.95671C21.3742 1.89618 22.2556 1.88105 23.1834 2.30476L23.7246 0.458999C23.2302 0.277494 22.5195 0 21.6542 0C18.918 0 17.0628 1.47072 17.0473 3.55891C17.0319 5.08726 18.3927 5.93466 19.4442 6.44917C20.5266 6.97881 20.8978 7.31172 20.8823 7.79595C20.8668 8.5223 19.9854 8.84008 19.1658 8.85521C18.0679 8.87035 17.3724 8.55257 16.8312 8.29532L16.2745 10.187C16.8003 10.4291 17.759 10.656 18.795 10.6711C21.7176 10.6711 23.5372 9.20326 23.5372 7.02604L23.1507 6.78393ZM30.4005 10.4594H33.0911L30.9262 0.170898H28.468C27.9423 0.170898 27.5093 0.473523 27.2774 0.942621L23.3671 10.4594H26.3825L26.9855 8.76457H30.0758L30.4005 10.4594ZM27.7876 6.55694L29.2875 2.36437L30.138 6.55694H27.7876ZM16.321 0.170898H13.6775L11.5287 10.4594H14.1723L16.321 0.170898Z" fill="#1434CB"/>
                </svg>
              </div>
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <h4 className="text-base font-medium text-gray-800 dark:text-white/90">Visa</h4>
                </div>
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  **** **** **** 4328 &nbsp;&nbsp;&nbsp; Expiry 01/25
                </p>
              </div>
            </div>
            <div className="mt-6 flex justify-end gap-2">
              <button className="rounded-md border border-gray-200 px-4 py-1.5 text-xs font-medium text-gray-600 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-300 dark:hover:bg-gray-800">
                Make Default
              </button>
              <button className="rounded-md border border-gray-200 px-4 py-1.5 text-xs font-medium text-gray-600 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-300 dark:hover:bg-gray-800">
                Edit
              </button>
              <button className="rounded-md border border-gray-200 px-4 py-1.5 text-xs font-medium text-gray-600 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-300 dark:hover:bg-gray-800">
                Delete
              </button>
            </div>
          </div>

          {/* Paypal */}
          <div className="flex flex-col justify-between rounded-xl border border-gray-200 p-5 dark:border-gray-800">
            <div className="flex gap-4">
              <div className="flex h-12 w-12 items-center justify-center rounded-lg border border-gray-100 bg-white dark:border-gray-800 dark:bg-gray-900 shadow-sm shrink-0">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M18.8475 7.15833C18.4233 4.20786 16.3274 2.87413 12.8719 2.87413H6.84021C6.44498 2.87413 6.10825 3.15585 6.04259 3.54146L3.25046 21.0543C3.18129 21.4886 3.52044 21.8741 3.96163 21.8741H7.81845C8.21369 21.8741 8.55041 21.5924 8.61608 21.2068L9.29413 16.9634C9.3598 16.5778 9.69652 16.2961 10.0918 16.2961H11.5303C15.3414 16.2961 18.2323 14.757 18.9056 10.5186C19.1026 9.27367 19.0664 8.16397 18.8475 7.15833Z" fill="#003087"/>
                  <path d="M17.4716 7.4243C17.0631 4.58284 15.0436 3.30396 11.7225 3.30396H6.18341C5.80373 3.30396 5.48003 3.5739 5.41684 3.94425L3.63935 15.0805C3.58231 15.4373 3.86561 15.7482 4.22915 15.7482H7.29177C7.67144 15.7482 7.99514 15.4783 8.05833 15.1079L8.74955 10.7712C8.81273 10.4009 9.13644 10.1309 9.51612 10.1309H11.1345C14.7715 10.1309 17.5305 8.66579 18.1729 4.6295C18.2612 4.07221 18.2561 3.5471 18.1589 3.0768C18.0667 3.96395 17.7289 5.63297 17.4716 7.4243Z" fill="#0079C1"/>
                </svg>
              </div>
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <h4 className="text-base font-medium text-gray-800 dark:text-white/90">Paypal</h4>
                </div>
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  name@example.com
                </p>
              </div>
            </div>
            <div className="mt-6 flex justify-end gap-2">
              <button className="rounded-md border border-gray-200 px-4 py-1.5 text-xs font-medium text-gray-600 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-300 dark:hover:bg-gray-800">
                Make Default
              </button>
              <button className="rounded-md border border-gray-200 px-4 py-1.5 text-xs font-medium text-gray-600 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-300 dark:hover:bg-gray-800">
                Edit
              </button>
              <button className="rounded-md border border-gray-200 px-4 py-1.5 text-xs font-medium text-gray-600 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-300 dark:hover:bg-gray-800">
                Delete
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
