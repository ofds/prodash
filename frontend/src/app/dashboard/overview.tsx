'use client';

import * as React from 'react';
import { Card, CardContent, Grid, InputAdornment, OutlinedInput, SvgIcon } from '@mui/material';
import { MagnifyingGlass as MagnifyingGlassIcon } from '@phosphor-icons/react';

import { searchProposals } from '@/services/api';
import { ProposalsTable } from '@/components/dashboard/overview/proposals-table';
import type { Proposal } from '@/components/dashboard/overview/proposals-table';

export default function Overview(): React.JSX.Element {
  const [proposals, setProposals] = React.useState<Proposal[]>([]);
  const [totalElements, setTotalElements] = React.useState(0);
  const [page, setPage] = React.useState(0);
  const [rowsPerPage, setRowsPerPage] = React.useState(10);
  const [searchTerm, setSearchTerm] = React.useState('');

  const fetchProposals = React.useCallback(async (term: string, p: number, rpp: number) => {
    try {
      const response = await searchProposals(term, p, rpp);
      setProposals(response.data.content);
      setTotalElements(response.data.totalElements);
    } catch (error) {
      console.error('Error fetching proposals:', error);
    }
  }, []);

  React.useEffect(() => {
    // Debounce search term to avoid excessive API calls
    const handler = setTimeout(() => {
      void fetchProposals(searchTerm, page, rowsPerPage);
    }, 500); // 500ms delay

    return () => {
      clearTimeout(handler);
    };
  }, [searchTerm, page, rowsPerPage, fetchProposals]);

  const handlePageChange = React.useCallback((event: React.MouseEvent<HTMLButtonElement> | null, newPage: number) => {
    setPage(newPage);
  }, []);

  const handleRowsPerPageChange = React.useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(Number.parseInt(event.target.value, 10));
    setPage(0);
  }, []);

  return (
    <Grid container spacing={3}>
      <Grid item xs={12}>
        <Card>
          <CardContent>
            <OutlinedInput
              defaultValue=""
              fullWidth
              placeholder="Search proposals by summary..."
              startAdornment={
                <InputAdornment position="start">
                  <SvgIcon color="action" fontSize="small">
                    <MagnifyingGlassIcon />
                  </SvgIcon>
                </InputAdornment>
              }
              sx={{ maxWidth: '500px' }}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </CardContent>
        </Card>
      </Grid>
      <Grid item xs={12}>
        <ProposalsTable // Renamed component
          proposals={proposals}
          count={totalElements}
          page={page}
          rowsPerPage={rowsPerPage}
          onPageChange={handlePageChange}
          onRowsPerPageChange={handleRowsPerPageChange}
          sx={{ height: '100%' }}
        />
      </Grid>
    </Grid>
  );
}