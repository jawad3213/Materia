import React, { useState } from "react";
import Checkbox from "../../form/input/Checkbox";

export default function ListWithCheckbox() {
  const [items, setItems] = useState([
    { id: 1, text: "Lorem ipsum dolor sit amet", checked: false },
    { id: 2, text: "It is a long established fact reader", checked: false },
    { id: 3, text: "Lorem ipsum dolor sit amet", checked: false },
    { id: 4, text: "Lorem ipsum dolor sit amet", checked: false },
    { id: 5, text: "Lorem ipsum dolor sit amet", checked: false },
  ]);

  const toggleItem = (index: number, checked: boolean) => {
    const newItems = [...items];
    newItems[index].checked = checked;
    setItems(newItems);
  };

  return (
    <ul className="flex flex-col rounded-xl border border-gray-200 dark:border-gray-800">
      {items.map((item, index) => (
        <li
          key={item.id}
          className="border-b border-gray-200 px-5 py-3 last:border-b-0 hover:bg-gray-100 dark:border-gray-800 dark:hover:bg-white/5"
        >
          <Checkbox
            checked={item.checked}
            onChange={(checked) => toggleItem(index, checked)}
            label={item.text}
          />
        </li>
      ))}
    </ul>
  );
}
