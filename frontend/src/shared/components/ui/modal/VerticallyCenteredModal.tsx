import React, { useState } from "react";
import { Modal } from "./index";
import Button from "../button/Button";

export default function VerticallyCenteredModal() {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <>
      <Button onClick={() => setIsOpen(true)}>Open Modal</Button>
      <Modal 
        isOpen={isOpen} 
        onClose={() => setIsOpen(false)} 
        showCloseButton={false}
        className="max-w-[600px] p-6 lg:p-10 text-center"
      >
        <div className="flex flex-col items-center gap-4">
          <h4 className="text-2xl md:text-[32px] md:leading-[1.2] font-bold text-gray-800 dark:text-white/90">
            All Done! Success Confirmed
          </h4>
          <p className="text-base text-gray-500 dark:text-gray-400">
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque euismod est quis mauris lacinia pharetra.
          </p>
          <div className="flex items-center justify-center gap-4 mt-2">
            <Button variant="outline" onClick={() => setIsOpen(false)}>
              Close
            </Button>
            <Button onClick={() => setIsOpen(false)}>
              Save Changes
            </Button>
          </div>
        </div>
      </Modal>
    </>
  );
}
