import React, { useState, useEffect } from "react";
import Box from "@mui/material/Box";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import CardHeader from "@mui/material/CardHeader";
import Typography from "@mui/material/Typography";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import TableContainer from "@mui/material/TableContainer";
import Chip from "@mui/material/Chip";
import Button from "@mui/material/Button";
import Alert from "@mui/material/Alert";
import LinearProgress from "@mui/material/LinearProgress";
import Paper from "@mui/material/Paper";
import Grid from "@mui/material/Grid";
import Divider from "@mui/material/Divider";
import Tooltip from "@mui/material/Tooltip";

import {
  Refresh as RefreshIcon,
  Error as AlertCircleIcon,
  CheckCircle as CheckCircleIcon,
  AccessTime as ClockIcon,
  Loop as LoaderIcon,
} from "@mui/icons-material";
import transactionsService from "../services/transactionsService";

const TransactionMonitor = ({walletAddress}) => {
  const [nftsToMint, setNftsToMint] = useState(0);
  const [nftsMinted, setNftsMinted] = useState(0);
  const [adaPrice, setAdaPrice] = useState(0);
  const [monitoringAddress, setMonitoringAddress] = useState("");

  const [transactions, setTransactions] = useState([]);

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(new Date());
  const [autoRefresh, setAutoRefresh] = useState(true);

const fetchTransactions = async () => {
    setIsLoading(true);
    setError(null);

  try {       
    const response = await transactionsService.getTransactions({ address: walletAddress });
    const data = response.data;

    setTransactions(data.transactions);
    setNftsToMint(data.totalNFTCount);
    setNftsMinted(data.mintedNFTCount);
    setAdaPrice(data.nftprice);
    setLastUpdated(new Date());
    setMonitoringAddress(data.monitoringAddress)

  } catch (err) {
    setError(err.response?.data?.message || err.message || "Unknown error occurred");
  } finally {
    setIsLoading(false);
  }
  };
  

useEffect(() => {
  if (!autoRefresh) {
    fetchTransactions();
  }
}, []);


useEffect(() => {
  if (!autoRefresh) return;
  let cancelled = false;

  const poll = async () => {
    await fetchTransactions();
    if (!cancelled) {
      setTimeout(poll, 50000);
    }
  };
  poll();

  return () => { cancelled = true; };
}, [autoRefresh]);


  const formatTimestamp = (timestamp) => {
    
// If its in seconds, convert to milliseconds
  if (timestamp < 1e12) {
    timestamp *= 1000;
  }

  const date = new Date(timestamp);
  return date.toLocaleString();
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case "Pending":
        return (
          <Chip icon={<ClockIcon fontSize="small" />} label="Pending" variant="outlined" size="small" />
        );
      case "Minting":
        return (
          <Chip icon={<LoaderIcon fontSize="small" className="animate-spin" />} label="Minting" color="primary" size="small" />
        );
      case "Minted":
        return (
          <Chip icon={<CheckCircleIcon fontSize="small" />} label="Minted" color="success" size="small" />
        );
      case "Failed":
        return (
          <Chip icon={<AlertCircleIcon fontSize="small" />} label="Failed" color="error" size="small" />
        );
      default:
        return <Chip label={status} size="small" />;
    }
  };

  const formatAda = (lovelace) => {
  return `${(lovelace / 1_000_000).toFixed(2)} ADA`;
};


  return (
    <Card sx={{ width: "100%", bgcolor: "background.paper" }}>
      <CardHeader
        title="Transaction Monitor"
          subheader={
    <Box sx={{
      display: "flex",
      alignItems: "center",
      flexWrap: "wrap",
      gap: 1,
      minWidth: 0,
    }}>
      <span style={{
        overflow: "hidden",
        textOverflow: "ellipsis",
        whiteSpace: "nowrap",

        display: "inline-block",
        verticalAlign: "middle"
      }}>
        <Tooltip title={monitoringAddress}>
        Monitoring wallet address: {monitoringAddress.slice(0, 14)}...{monitoringAddress.slice(-6)}
        </Tooltip>

      </span>
    </Box>
  }
        action={
          <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
            <Button variant="outlined" size="medium" onClick={() => setAutoRefresh(!autoRefresh)}>
              {autoRefresh ? "Auto-refresh On" : "Auto-refresh Off"}
            </Button>
            <Button
              variant="outlined"
              size="small"
              onClick={fetchTransactions}
              disabled={isLoading}
              sx={{ minWidth: 0, p: 1 }}
            >
              {isLoading ? (
                <LoaderIcon fontSize="small" className="animate-spin" />
              ) : (
                <RefreshIcon fontSize="small" />
              )}
            </Button>
          </Box>
        }
      />
      <Typography variant="caption" color="text.secondary" sx={{ px: 2, pb: 2, display: "block" }}>
        Last updated: {lastUpdated.toLocaleString()}
      </Typography>

      {/* Updated NFT Minting Stats */}
      <Box sx={{ px: 2, pb: 2 }}>
        <Paper elevation={2} sx={{ p: 2, display: "flex", flexDirection: "column", alignItems: "center" }}>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={4} >
              <Typography variant="subtitle2" color="text.secondary">
                NFTs in Collection:
              </Typography>
              <Typography variant="h6" color="text.primary">
                {nftsToMint}
              </Typography>
            </Grid>
            <Grid item xs={4}>
              <Typography variant="subtitle2" color="text.secondary">
                NFTs Minted:
              </Typography>
              <Typography variant="h6" color="text.primary">
                {nftsMinted} / {nftsToMint}
              </Typography>
            </Grid>
            <Grid item xs={4}>
              <Typography variant="subtitle2" color="text.secondary">
                Price per NFT:
              </Typography>
              <Typography variant="h6" color="text.primary">
                {adaPrice} ADA
              </Typography>
            </Grid>
          </Grid>
            <Box sx={{ width: "100%", mt: 2 }}>
            <LinearProgress
              variant="determinate"
              value={nftsToMint > 0 ? (nftsMinted / nftsToMint) * 100 : 0}
              sx={{
                    '& .MuiLinearProgress-bar': {
                      backgroundColor: '#d32f2f', // Material-UI red[700]
                    },
                    backgroundColor: '#ffebee', // Material-UI red[50]
                  }}
            />
          </Box>
        </Paper>
      </Box>

      <Divider sx={{ mx: 2, mb: 2 }} />

      <CardContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {isLoading && (
          <Box sx={{ mb: 2 }}>
            <LinearProgress />
          </Box>
        )}

        <TableContainer component={Paper} variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Sender Address</TableCell>
                <TableCell>Amount Sent</TableCell>
                <TableCell>Timestamp</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Refund</TableCell>
                <TableCell>NFTs Minted </TableCell>
                <TableCell>NFTs To mint</TableCell>
                <TableCell>Transaction Hash</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {transactions.length > 0 ? (
                transactions.map((tx) => (
                  <TableRow key={tx.id}>
                  <TableCell sx={{ fontFamily: "monospace", fontSize: "0.75rem" }}>
                    <Tooltip title={tx.senderAddress}>
                      <span>{tx.senderAddress.slice(0, 10)}...{tx.senderAddress.slice(-6)}</span>
                    </Tooltip>
                  </TableCell>

                    <TableCell>{formatAda(tx.amount)}</TableCell>
                    <TableCell sx={{ minWidth: "200px", fontSize: "0.85rem"}}>{formatTimestamp(tx.blockTime)}</TableCell>
                    <TableCell>{getStatusBadge(tx.status)}</TableCell>
                    <TableCell>
                      {tx.refund > 0 ? (
                        <Tooltip title={tx.refunded === true ? "Refunded" : "Not refunded"}>
                          <Chip
                            label={formatAda(tx.refund)}
                            color={tx.refunded === true ? "success" : "error"}
                            size="small"
                            variant="filled"
                          />
                        </Tooltip>
                      ) : (
                        "---"
                      )}
                    </TableCell>

                    <TableCell>{tx.amountMinted}</TableCell>
                    <TableCell>{tx.amountToMint}</TableCell>
                    <TableCell sx={{ fontFamily: "monospace", fontSize: "0.75rem" }}>
                      <Tooltip title={tx.txHash}>
                        <span>{tx.txHash.slice(0, 6)}...{tx.txHash.slice(-4)}</span>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 3, color: "text.secondary" }}>
                    No transactions found
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </CardContent>
    </Card>
  );
};

export default TransactionMonitor;