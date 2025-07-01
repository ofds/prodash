'use client';

import * as React from 'react';
import { Grid } from '@mui/system';

import { getRankedProposals } from '@/services/api';
import { RankedProposals } from '@/components/dashboard/overview/ranked-proposals';
import type { Proposal } from '@/components/dashboard/overview/ranked-proposals';

export default function Overview(): React.JSX.Element {
  const [proposals, setProposals] = React.useState<Proposal[]>([]);

  React.useEffect(() => {
    const fetchProposals = async () => {
      try {
        const response = await getRankedProposals();
        setProposals(response.data);
      } catch (error) {
        console.error('Error fetching proposals:', error);
      }
    };

    void fetchProposals();
  }, []);

  return (
    <Grid container spacing={3}>
  {/* The "item" prop is no longer needed */}
  <Grid xs={12}>
    <RankedProposals proposals={proposals} sx={{ height: '100%' }} />
  </Grid>
</Grid>
  );
}