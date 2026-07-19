import type { Metadata } from "next";
import type { ReactNode } from "react";

import "../src/_app/styles/globals.css";

export const metadata: Metadata = {
  title: "Kafka AZ Post Workspace",
  description: "Kafka AZ backend API post workspace",
};

export default function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
