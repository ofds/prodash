import * as React from 'react';
import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import CardHeader from '@mui/material/CardHeader';
import Divider from '@mui/material/Divider';
import type { SxProps } from '@mui/material/styles';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';

export interface Proposal {
  id: string;
  siglaTipo: string;
  numero: number;
  ano: number;
  ementa: string;
  impactoScoreLLM: number;
}

export interface RankedProposalsProps {
  proposals?: Proposal[];
  sx?: SxProps;
}

export function RankedProposals({ proposals = [], sx }: RankedProposalsProps): React.JSX.Element {
  return (
    <Card sx={sx}>
      <CardHeader title="Today's Legislative Proposals by Impact" />
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
                <TableCell align="right">{proposal.impactoScoreLLM}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Box>
    </Card>
  );
}