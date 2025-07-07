import * as React from 'react';
import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import CardHeader from '@mui/material/CardHeader';
import Divider from '@mui/material/Divider';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import TablePagination from '@mui/material/TablePagination';
import type { SxProps } from '@mui/material/styles';

export interface Proposal {
  id: string;
  siglaTipo: string;
  numero: number;
  ano: number;
  ementa: string;
  impactScore: number;
}

// Corrected prop types for pagination handlers
export interface ProposalsTableProps {
  proposals: Proposal[];
  count: number;
  page: number;
  rowsPerPage: number;
  onPageChange: (event: React.MouseEvent<HTMLButtonElement> | null, newPage: number) => void;
  onRowsPerPageChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  sx?: SxProps;
}

// Renamed component
export function ProposalsTable({
  proposals = [],
  sx,
  count,
  page,
  rowsPerPage,
  onPageChange,
  onRowsPerPageChange,
}: ProposalsTableProps): React.JSX.Element {
  return (
    <Card sx={sx}>
      <CardHeader title="Legislative Proposals" />
      <Divider />
      <Box sx={{ overflowX: 'auto' }}>
        <Table sx={{ minWidth: 800 }}>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Proposal</TableCell>
              <TableCell>Summary</TableCell>
              <TableCell align="right">Impact Score</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {proposals.map((proposal) => (
              <TableRow hover key={proposal.id}>
                <TableCell>{proposal.id}</TableCell>
                <TableCell>{`${proposal.siglaTipo} ${proposal.numero}/${proposal.ano}`}</TableCell>
                <TableCell>{proposal.ementa}</TableCell>
                <TableCell align="right">{proposal.impactScore}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Box>
      <TablePagination
        component="div"
        count={count}
        onPageChange={onPageChange} // This will now work without error
        onRowsPerPageChange={onRowsPerPageChange}
        page={page}
        rowsPerPage={rowsPerPage}
        rowsPerPageOptions={[5, 10, 25]}
      />
    </Card>
  );
}