'use client';

import * as React from 'react';
import {
  Card,
  CardContent,
  Grid,
  InputAdornment,
  OutlinedInput,
  SvgIcon,
  Typography,
  Slider, // 1. Import Slider and Typography
} from '@mui/material';
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

  // 2. Add state for sorting and impact score filter
  const [sort, setSort] = React.useState<{ sortBy: string; order: 'asc' | 'desc' }>({
    sortBy: 'presentationDate',
    order: 'desc',
  });
  const [impactScore, setImpactScore] = React.useState<number>(0);

  // 3. Update fetchProposals to accept sort and filter parameters
  const fetchProposals = React.useCallback(
    async (
      term: string,
      p: number,
      rpp: number,
      s: { sortBy: string; order: 'asc' | 'desc' },
      minImpact: number
    ) => {
      try {
        const sortString = `${s.sortBy},${s.order}`;
        const response = await searchProposals(term, p, rpp, sortString, minImpact);
        setProposals(response.data.content);
        setTotalElements(response.data.totalElements);
      } catch (error) {
        console.error('Error fetching proposals:', error);
      }
    },
    []
  );

  React.useEffect(() => {
    const handler = setTimeout(() => {
      // 4. Pass sort and filter state to the API call
      void fetchProposals(searchTerm, page, rowsPerPage, sort, impactScore);
    }, 500);

    return () => {
      clearTimeout(handler);
    };
  }, [searchTerm, page, rowsPerPage, sort, impactScore, fetchProposals]);

  const handlePageChange = React.useCallback(
    (event: React.MouseEvent<HTMLButtonElement> | null, newPage: number) => {
      setPage(newPage);
    },
    []
  );

  const handleRowsPerPageChange = React.useCallback(
    (event: React.ChangeEvent<HTMLInputElement>) => {
      setRowsPerPage(Number.parseInt(event.target.value, 10));
      setPage(0);
    },
    []
  );

  // 5. Add handlers for sorting and filtering
  const handleSort = (newSortBy: string) => {
    setSort((prevSort) => {
      const isAsc = prevSort.sortBy === newSortBy && prevSort.order === 'asc';
      return { sortBy: newSortBy, order: isAsc ? 'desc' : 'asc' };
    });
  };

  const handleFilterChange = (event: Event, newValue: number | number[]) => {
    setImpactScore(newValue as number);
  };

  return (
    <Grid container spacing={3}>
      <Grid item xs={12}>
        <Card>
          <CardContent>
            {/* 6. Add UI for search and filtering */}
            <Grid container spacing={4} alignItems="center">
              <Grid item xs={12} md={6}>
                <OutlinedInput
                  defaultValue=""
                  fullWidth
                  placeholder="Pesquisar propostas por ementa..."
                  startAdornment={
                    <InputAdornment position="start">
                      <SvgIcon color="action" fontSize="small">
                        <MagnifyingGlassIcon />
                      </SvgIcon>
                    </InputAdornment>
                  }
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Typography id="input-slider" gutterBottom>
                  Pontuação de Impacto Mínima
                </Typography>
                <Slider
                  value={impactScore}
                  onChange={handleFilterChange}
                  aria-labelledby="input-slider"
                  valueLabelDisplay="auto"
                  step={0.5}
                  marks
                  min={0}
                  max={10}
                />
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      </Grid>
      <Grid item xs={12}>
        {/* 7. Pass sort props to the ProposalsTable */}
        <ProposalsTable
          proposals={proposals}
          count={totalElements}
          page={page}
          rowsPerPage={rowsPerPage}
          onPageChange={handlePageChange}
          onRowsPerPageChange={handleRowsPerPageChange}
          sort={sort}
          onSort={handleSort}
          sx={{ height: '100%' }}
        />
      </Grid>
    </Grid>
  );
}