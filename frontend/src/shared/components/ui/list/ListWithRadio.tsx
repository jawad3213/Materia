import React, { useState } from "react";
import Radio from "../../form/input/Radio";

export default function ListWithRadio() {
  const [selected, setSelected] = useState("item-1");

  const items = [
    { id: "item-1", text: "Lorem ipsum dolor sit amet" },
    { id: "item-2", text: "It is a long established fact reader" },
    { id: "item-3", text: "Lorem ipsum dolor sit amet" },
    { id: "item-4", text: "Lorem ipsum dolor sit amet" },
    { id: "item-5", text: "Lorem ipsum dolor sit amet" },
  ];

  return (
    <ul className="flex flex-col rounded-xl border border-gray-200 dark:border-gray-800">
      {items.map((item) => (
        <li
          key={item.id}
          className="border-b border-gray-200 px-5 py-3 last:border-b-0 hover:bg-gray-100 dark:border-gray-800 dark:hover:bg-white/5"
        >
          <Radio
            id={item.id}
            name="list-radio"
            value={item.id}
            checked={selected === item.id}
            onChange={(value) => setSelected(value)}
            label={item.text}
          />
        </li>
      ))}
    </ul>
  );
}
