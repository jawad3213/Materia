import React, { useState } from "react";
import { Modal } from "./index";
import Button from "../button/Button";
import Input from "../../form/input/InputField";
import Label from "../../form/Label";

export default function FormInModal() {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <>
      <Button onClick={() => setIsOpen(true)}>Open Modal</Button>
      <Modal isOpen={isOpen} onClose={() => setIsOpen(false)} className="max-w-[700px] p-6 lg:p-10">
        <div className="flex flex-col gap-6">
          <h4 className="text-lg font-bold text-gray-800 dark:text-white/90">
            Personal Information
          </h4>
          
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
            <div>
              <Label>First Name</Label>
              <Input type="text" defaultValue="Emirhan" />
            </div>
            <div>
              <Label>Last Name</Label>
              <Input type="text" defaultValue="Boruch" />
            </div>
          </div>
          
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
            <div>
              <Label>Last Name</Label>
              <Input type="email" defaultValue="emirhanboruch55@gmail.com" />
            </div>
            <div>
              <Label>Phone</Label>
              <Input type="text" defaultValue="+09 363 398 46" />
            </div>
          </div>
          
          <div>
            <Label>Bio</Label>
            <Input type="text" defaultValue="Team Manager" />
          </div>

          <div className="flex items-center justify-end gap-3 mt-2">
            <Button size="sm" variant="outline" onClick={() => setIsOpen(false)}>
              Close
            </Button>
            <Button size="sm" onClick={() => setIsOpen(false)}>
              Save Changes
            </Button>
          </div>
        </div>
      </Modal>
    </>
  );
}
