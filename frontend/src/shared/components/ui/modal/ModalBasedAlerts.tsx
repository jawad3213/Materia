import React, { useState } from "react";
import { Modal } from "./index";

export default function ModalBasedAlerts() {
  const [activeModal, setActiveModal] = useState<
    "success" | "info" | "warning" | "error" | null
  >(null);

  const openModal = (type: "success" | "info" | "warning" | "error") => {
    setActiveModal(type);
  };

  const closeModal = () => {
    setActiveModal(null);
  };

  const icons = {
    success: (
      <svg
        className="fill-current w-9 h-9"
        viewBox="0 0 24 24"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          fillRule="evenodd"
          clipRule="evenodd"
          d="M3.70186 12.0001C3.70186 7.41711 7.41711 3.70186 12.0001 3.70186C16.5831 3.70186 20.2984 7.41711 20.2984 12.0001C20.2984 16.5831 16.5831 20.2984 12.0001 20.2984C7.41711 20.2984 3.70186 16.5831 3.70186 12.0001ZM12.0001 1.90186C6.423 1.90186 1.90186 6.423 1.90186 12.0001C1.90186 17.5772 6.423 22.0984 12.0001 22.0984C17.5772 22.0984 22.0984 17.5772 22.0984 12.0001C22.0984 6.423 17.5772 1.90186 12.0001 1.90186ZM15.6197 10.7395C15.9712 10.388 15.9712 9.81819 15.6197 9.46672C15.2683 9.11525 14.6984 9.11525 14.347 9.46672L11.1894 12.6243L9.6533 11.0883C9.30183 10.7368 8.73198 10.7368 8.38051 11.0883C8.02904 11.4397 8.02904 12.0096 8.38051 12.3611L10.553 14.5335C10.7217 14.7023 10.9507 14.7971 11.1894 14.7971C11.428 14.7971 11.657 14.7023 11.8257 14.5335L15.6197 10.7395Z"
        />
      </svg>
    ),
    error: (
      <svg
        className="fill-current w-9 h-9"
        viewBox="0 0 24 24"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          fillRule="evenodd"
          clipRule="evenodd"
          d="M20.3499 12.0004C20.3499 16.612 16.6115 20.3504 11.9999 20.3504C7.38832 20.3504 3.6499 16.612 3.6499 12.0004C3.6499 7.38881 7.38833 3.65039 11.9999 3.65039C16.6115 3.65039 20.3499 7.38881 20.3499 12.0004ZM11.9999 22.1504C17.6056 22.1504 22.1499 17.6061 22.1499 12.0004C22.1499 6.3947 17.6056 1.85039 11.9999 1.85039C6.39421 1.85039 1.8499 6.3947 1.8499 12.0004C1.8499 17.6061 6.39421 22.1504 11.9999 22.1504ZM13.0008 16.4753C13.0008 15.923 12.5531 15.4753 12.0008 15.4753L11.9998 15.4753C11.4475 15.4753 10.9998 15.923 10.9998 16.4753C10.9998 17.0276 11.4475 17.4753 11.9998 17.4753L12.0008 17.4753C12.5531 17.4753 13.0008 17.0276 13.0008 16.4753ZM11.9998 6.62898C12.414 6.62898 12.7498 6.96476 12.7498 7.37898L12.7498 13.0555C12.7498 13.4697 12.414 13.8055 11.9998 13.8055C11.5856 13.8055 11.2498 13.4697 11.2498 13.0555L11.2498 7.37898C11.2498 6.96476 11.5856 6.62898 11.9998 6.62898Z"
        />
      </svg>
    ),
    warning: (
      <svg
        className="fill-current w-9 h-9"
        viewBox="0 0 24 24"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          fillRule="evenodd"
          clipRule="evenodd"
          d="M3.6501 12.0001C3.6501 7.38852 7.38852 3.6501 12.0001 3.6501C16.6117 3.6501 20.3501 7.38852 20.3501 12.0001C20.3501 16.6117 16.6117 20.3501 12.0001 20.3501C7.38852 20.3501 3.6501 16.6117 3.6501 12.0001ZM12.0001 1.8501C6.39441 1.8501 1.8501 6.39441 1.8501 12.0001C1.8501 17.6058 6.39441 22.1501 12.0001 22.1501C17.6058 22.1501 22.1501 17.6058 22.1501 12.0001C22.1501 6.39441 17.6058 1.8501 12.0001 1.8501ZM10.9992 7.52517C10.9992 8.07746 11.4469 8.52517 11.9992 8.52517H12.0002C12.5525 8.52517 13.0002 8.07746 13.0002 7.52517C13.0002 6.97289 12.5525 6.52517 12.0002 6.52517H11.9992C11.4469 6.52517 10.9992 6.97289 10.9992 7.52517ZM12.0002 17.3715C11.586 17.3715 11.2502 17.0357 11.2502 16.6215V10.945C11.2502 10.5308 11.586 10.195 12.0002 10.195C12.4144 10.195 12.7502 10.5308 12.7502 10.945V16.6215C12.7502 17.0357 12.4144 17.3715 12.0002 17.3715Z"
        />
      </svg>
    ),
    info: (
      <svg
        className="fill-current w-9 h-9"
        viewBox="0 0 24 24"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          fillRule="evenodd"
          clipRule="evenodd"
          d="M3.6501 11.9996C3.6501 7.38803 7.38852 3.64961 12.0001 3.64961C16.6117 3.64961 20.3501 7.38803 20.3501 11.9996C20.3501 16.6112 16.6117 20.3496 12.0001 20.3496C7.38852 20.3496 3.6501 16.6112 3.6501 11.9996ZM12.0001 1.84961C6.39441 1.84961 1.8501 6.39392 1.8501 11.9996C1.8501 17.6053 6.39441 22.1496 12.0001 22.1496C17.6058 22.1496 22.1501 17.6053 22.1501 11.9996C22.1501 6.39392 17.6058 1.84961 12.0001 1.84961ZM10.9992 7.52468C10.9992 8.07697 11.4469 8.52468 11.9992 8.52468H12.0002C12.5525 8.52468 13.0002 8.07697 13.0002 7.52468C13.0002 6.9724 12.5525 6.52468 12.0002 6.52468H11.9992C11.4469 6.52468 10.9992 6.9724 10.9992 7.52468ZM12.0002 17.371C11.586 17.371 11.2502 17.0352 11.2502 16.621V10.9445C11.2502 10.5303 11.586 10.1945 12.0002 10.1945C12.4144 10.1945 12.7502 10.5303 12.7502 10.9445V16.621C12.7502 17.0352 12.4144 17.371 12.0002 17.371Z"
        />
      </svg>
    ),
  };

  const WavyBg = ({ className }: { className: string }) => (
    <svg className={`absolute w-full h-full ${className}`} viewBox="0 0 100 100" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
       <path d="M50 0C55 0 59 2.5 62 6.5L66 11.5C68.5 14.5 73.5 15.5 77.5 14.5L83.5 13C88.5 11.5 93.5 14.5 95 19.5L96.5 25.5C97.5 29.5 100.5 32 100 36.5L100 42.5C99 47 100.5 51 98.5 54.5L95.5 59.5C92.5 63 92 68.5 94.5 72.5L97.5 77.5C100.5 82 98.5 87.5 93.5 89.5L88 91.5C84 93 81.5 96.5 80.5 100H50H19.5C18.5 96.5 16 93 12 91.5L6.5 89.5C1.5 87.5-0.5 82 2.5 77.5L5.5 72.5C8 68.5 7.5 63 4.5 59.5L1.5 54.5C-0.5 51 1 47 0 42.5V36.5C-0.5 32 2.5 29.5 3.5 25.5L5 19.5C6.5 14.5 11.5 11.5 16.5 13L22.5 14.5C26.5 15.5 31.5 14.5 34 11.5L38 6.5C41 2.5 45 0 50 0Z" />
    </svg>
  );

  const alerts = {
    success: {
      title: "Well Done!",
      message:
        "Lorem ipsum dolor sit amet consectetur. Feugiat ipsum libero tempor felis risus nisi non. Quisque eu ut tempor curabitur.",
      buttonText: "Okay, Got It",
      icon: icons.success,
      iconColor: "text-success-500",
      iconBg: "text-success-50 dark:text-success-500/15",
      btnClass: "bg-success-500 hover:bg-success-600 text-white",
    },
    info: {
      title: "Information Alert!",
      message:
        "Lorem ipsum dolor sit amet consectetur. Feugiat ipsum libero tempor felis risus nisi non. Quisque eu ut tempor curabitur.",
      buttonText: "Okay, Got It",
      icon: icons.info,
      iconColor: "text-[#0096EB]",
      iconBg: "text-[#0096EB]/10 dark:text-[#0096EB]/20",
      btnClass: "bg-[#0096EB] hover:bg-[#0080C9] text-white",
    },
    warning: {
      title: "Warning Alert!",
      message:
        "Lorem ipsum dolor sit amet consectetur. Feugiat ipsum libero tempor felis risus nisi non. Quisque eu ut tempor curabitur.",
      buttonText: "Okay, Got It",
      icon: icons.warning,
      iconColor: "text-warning-500",
      iconBg: "text-warning-50 dark:text-warning-500/15",
      btnClass: "bg-warning-500 hover:bg-warning-600 text-white",
    },
    error: {
      title: "Danger Alert!",
      message:
        "Lorem ipsum dolor sit amet consectetur. Feugiat ipsum libero tempor felis risus nisi non. Quisque eu ut tempor curabitur.",
      buttonText: "Okay, Got It",
      icon: icons.error,
      iconColor: "text-error-500",
      iconBg: "text-error-50 dark:text-error-500/15",
      btnClass: "bg-error-500 hover:bg-error-600 text-white",
    },
  };

  const activeAlert = activeModal ? alerts[activeModal] : null;

  return (
    <>
      <div className="flex flex-wrap items-center gap-4">
        <button
          onClick={() => openModal("success")}
          className="px-5 py-3 text-sm font-medium text-white transition rounded-lg bg-success-500 hover:bg-success-600"
        >
          Success Alert
        </button>
        <button
          onClick={() => openModal("info")}
          className="px-5 py-3 text-sm font-medium text-white transition rounded-lg bg-[#0096EB] hover:bg-[#0080C9]"
        >
          Info Alert
        </button>
        <button
          onClick={() => openModal("warning")}
          className="px-5 py-3 text-sm font-medium text-white transition rounded-lg bg-warning-500 hover:bg-warning-600"
        >
          Warning Alert
        </button>
        <button
          onClick={() => openModal("error")}
          className="px-5 py-3 text-sm font-medium text-white transition rounded-lg bg-error-500 hover:bg-error-600"
        >
          Danger Alert
        </button>
      </div>

      <Modal
        isOpen={!!activeModal}
        onClose={closeModal}
        className="max-w-[600px] p-6 lg:p-10 text-center"
      >
        {activeAlert && (
          <div className="flex flex-col items-center gap-4">
            <div className={`relative flex items-center justify-center w-[84px] h-[84px] mb-1 ${activeAlert.iconColor}`}>
              <WavyBg className={activeAlert.iconBg} />
              <div className="relative z-10">{activeAlert.icon}</div>
            </div>

            <h4 className="text-2xl md:text-[32px] md:leading-[1.2] font-bold text-gray-800 dark:text-white/90">
              {activeAlert.title}
            </h4>

            <p className="text-base text-gray-500 dark:text-gray-400">
              {activeAlert.message}
            </p>

            <button
              onClick={closeModal}
              className={`px-6 py-3 mt-2 text-base font-medium transition rounded-lg ${activeAlert.btnClass}`}
            >
              {activeAlert.buttonText}
            </button>
          </div>
        )}
      </Modal>
    </>
  );
}
