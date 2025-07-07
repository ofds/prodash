// frontend/src/components/dashboard/overview/proposals-table.tsx

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
import {
  Collapse,
  IconButton,
  Typography,
  List,
  ListItem,
  ListItemText,
  Link,
} from '@mui/material';
import { KeyboardArrowDown as KeyboardArrowDownIcon, KeyboardArrowUp as KeyboardArrowUpIcon } from '@mui/icons-material';

// [INICIO] Atualize a interface para incluir todos os campos da API
export interface Proposal {
  id: string;
  uri: string;
  title: string;
  siglaTipo: string;
  descricaoTipo: string;
  numero: number;
  ano: number;
  summary: string;
  ementa: string;
  ementaDetalhada: string | null;
  fullTextUrl: string;
  uriAutores: string;
  status: string;
  situation: string | null;
  presentationDate: string;
  impactScore: number;
  justification: string;
  dispatch: string;
  processingAgency: string;
  authors: { name: string; type: string }[];
  themes: any[];
}
// [FIM]

export interface ProposalsTableProps {
  proposals: Proposal[];
  count: number;
  page: number;
  rowsPerPage: number;
  onPageChange: (
    event: React.MouseEvent<HTMLButtonElement> | null,
    newPage: number
  ) => void;
  onRowsPerPageChange: (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => void;
  sx?: SxProps;
}
// [INICIO] Crie um componente de linha expansível
function ProposalRow({ proposal }: { proposal: Proposal }) {
  const [open, setOpen] = React.useState(false);

  return (
    <React.Fragment>
      <TableRow hover>
        <TableCell>
          <IconButton
            aria-label="expand row"
            size="small"
            onClick={() => setOpen(!open)}
          >
            {open ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
          </IconButton>
        </TableCell>
        <TableCell>{proposal.id}</TableCell>
        <TableCell>{`${proposal.siglaTipo} ${proposal.numero}/${proposal.ano}`}</TableCell>
        <TableCell>{proposal.ementa}</TableCell>
        <TableCell align="right">{proposal.impactScore}</TableCell>
      </TableRow>
      <TableRow>
        <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={6}>
          <Collapse in={open} timeout="auto" unmountOnExit>
            <Box sx={{ margin: 1 }}>
              <Typography variant="h6" gutterBottom component="div">
                Detalhes
              </Typography>
              <List dense>
                <ListItem>
                  <ListItemText primary="Título" secondary={proposal.title} />
                </ListItem>
                <ListItem>
                  <ListItemText primary="Resumo" secondary={proposal.summary} />
                </ListItem>
                <ListItem>
                  <ListItemText
                    primary="Justificativa"
                    secondary={proposal.justification}
                  />
                </ListItem>
                <ListItem>
                  <ListItemText primary="Situação" secondary={proposal.status} />
                </ListItem>
                <ListItem>
                  <ListItemText
                    primary="Data de Apresentação"
                    secondary={new Date(
                      proposal.presentationDate
                    ).toLocaleDateString()}
                  />
                </ListItem>
                <ListItem>
                  <ListItemText
                    primary="Órgão"
                    secondary={proposal.processingAgency}
                  />
                </ListItem>
                <ListItem>
                  <ListItemText
                    primary="Autores"
                    secondary={proposal.authors
                      .map((author) => author.name)
                      .join(', ')}
                  />
                </ListItem>
                <ListItem>
                  <ListItemText
                    primary="Íntegra"
                    secondary={
                      <Link
                        href={proposal.fullTextUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        Link para o texto completo
                      </Link>
                    }
                  />
                </ListItem>
              </List>
            </Box>
          </Collapse>
        </TableCell>
      </TableRow>
    </React.Fragment>
  );
}
// [FIM]

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
      <CardHeader title="Propostas Legislativas" />
      <Divider />
      <Box sx={{ overflowX: 'auto' }}>
        <Table sx={{ minWidth: 800 }}>
          <TableHead>
            {/* [INICIO] Adicione uma coluna para o botão de expansão */}
            <TableRow>
              <TableCell />
              <TableCell>ID</TableCell>
              <TableCell>Proposta</TableCell>
              <TableCell>Ementa</TableCell>
              <TableCell align="right">Pontuação de Impacto</TableCell>
            </TableRow>
            {/* [FIM] */}
          </TableHead>
          <TableBody>
            {/* [INICIO] Renderize as linhas expansíveis */}
            {proposals.map((proposal) => (
              <ProposalRow key={proposal.id} proposal={proposal} />
            ))}
            {/* [FIM] */}
          </TableBody>
        </Table>
      </Box>
      <TablePagination
        component="div"
        count={count}
        onPageChange={onPageChange}
        onRowsPerPageChange={onRowsPerPageChange}
        page={page}
        rowsPerPage={rowsPerPage}
        rowsPerPageOptions={[5, 10, 25]}
      />
    </Card>
  );
}