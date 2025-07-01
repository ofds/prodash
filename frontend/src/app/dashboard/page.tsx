import * as React from 'react';
import type { Metadata } from 'next';

import Overview from '@/app/dashboard/overview';

export const metadata = { title: `Overview | Dashboard` } satisfies Metadata;

export default function Page(): React.JSX.Element {
  return <Overview />;
}