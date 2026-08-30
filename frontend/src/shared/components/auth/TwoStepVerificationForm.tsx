import React, { useRef, useState } from "react";
import { Link } from "react-router";
import { ChevronLeftIcon } from "../../icons";
import Label from "../form/Label";
import Button from "../ui/button/Button";

export default function TwoStepVerificationForm() {
  const [code, setCode] = useState(["", "", "", "", "", ""]);
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  const handleChange = (index: number, value: string) => {
    if (!/^\d*$/.test(value)) return;

    const newCode = [...code];
    newCode[index] = value;
    setCode(newCode);

    if (value && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Backspace" && !code[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handlePaste = (e: React.ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData("text").slice(0, 6).replace(/\D/g, "");
    if (pastedData) {
      const newCode = [...code];
      for (let i = 0; i < pastedData.length; i++) {
        if (i < 6) {
          newCode[i] = pastedData[i];
        }
      }
      setCode(newCode);
      const nextIndex = Math.min(pastedData.length, 5);
      inputRefs.current[nextIndex]?.focus();
    }
  };

  return (
    <div className="flex flex-col flex-1 w-full h-full overflow-hidden">
      <div className="w-full max-w-md pt-10 mx-auto px-6 sm:px-0">
        <Link
          to="/"
          className="inline-flex items-center text-sm text-gray-500 transition-colors hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300"
        >
          <ChevronLeftIcon className="size-5" />
          Back to dashboard
        </Link>
      </div>
      <div className="flex flex-col justify-center flex-1 w-full max-w-md mx-auto px-6 sm:px-0 pb-10">
        <div>
          <div className="mb-5 sm:mb-8">
            <h1 className="mb-2 font-semibold text-gray-800 text-title-sm dark:text-white/90 sm:text-title-md">
              Two Step Verification
            </h1>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              A verification code has been sent to your mobile. Please enter it in the field below.
            </p>
          </div>
          <div>
            <form>
              <div className="space-y-6">
                <div>
                  <Label>Type your 6 digits security code</Label>
                  <div className="flex items-center gap-2 sm:gap-4 mt-3">
                    {code.map((digit, index) => (
                      <input
                        key={index}
                        type="text"
                        maxLength={1}
                        value={digit}
                        ref={(el) => (inputRefs.current[index] = el)}
                        onChange={(e) => handleChange(index, e.target.value)}
                        onKeyDown={(e) => handleKeyDown(index, e)}
                        onPaste={handlePaste}
                        className="w-12 h-12 text-center text-2xl font-medium text-gray-800 bg-transparent border border-gray-300 rounded-lg sm:w-14 sm:h-14 dark:border-gray-700 dark:text-white focus:border-brand-500 focus:outline-none dark:focus:border-brand-500"
                      />
                    ))}
                  </div>
                </div>
                <div>
                  <Button className="w-full" size="sm">
                    Verify My Account
                  </Button>
                </div>
              </div>
            </form>

            <div className="mt-5">
              <p className="text-sm font-normal text-gray-700 dark:text-gray-400">
                Didn't get the code?{" "}
                <button
                  type="button"
                  className="text-brand-500 hover:text-brand-600 dark:text-brand-400 hover:underline"
                >
                  Resend
                </button>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
