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
import Tooltip from "@mui/material/Tooltip";
import { alpha, useTheme } from "@mui/material/styles";

import {
  Refresh as RefreshIcon,
  Error as AlertCircleIcon,
  CheckCircle as CheckCircleIcon,
  AccessTime as ClockIcon,
  Loop as LoaderIcon,
  Wallet as WalletIcon,
  TrendingUp as TrendingUpIcon,
  AccountBalanceWallet as BalanceIcon,
  Token as TokenIcon,
} from "@mui/icons-material";
import transactionsService from "../services/transactionsService";

const TransactionMonitor = ({walletAddress}) => {
  const theme = useTheme();
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

    // Sort transactions by blockTime
    const sortedTransactions = data.transactions.sort((a, b) => {
      
      const timeA = a.blockTime < 1e12 ? a.blockTime * 1000 : a.blockTime;
      const timeB = b.blockTime < 1e12 ? b.blockTime * 1000 : b.blockTime;
      return timeA - timeB; // Sort
    });

    setTransactions(sortedTransactions);
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
          <Chip 
            icon={<ClockIcon fontSize="small" />} 
            label="Pending" 
            variant="outlined" 
            size="small"
            sx={{
              bgcolor: alpha(theme.palette.warning.main, 0.1),
              color: 'warning.main',
              borderColor: 'warning.main',
              fontWeight: 600,
            }}
          />
        );
      case "Minting":
        return (
          <Chip 
            icon={
              <LoaderIcon 
                fontSize="small" 
                sx={{
                  animation: 'spin 1s linear infinite',
                  '@keyframes spin': {
                    '0%': {
                      transform: 'rotate(0deg)',
                    },
                    '100%': {
                      transform: 'rotate(360deg)',
                    },
                  },
                }}
              />
            } 
            label="Minting" 
            color="primary" 
            size="small"
            sx={{
              bgcolor: alpha(theme.palette.primary.main, 0.1),
              color: 'primary.main',
              fontWeight: 600,
            }}
          />
        );
      case "Minted":
        return (
          <Chip 
            icon={<CheckCircleIcon fontSize="small" />} 
            label="Minted" 
            color="success" 
            size="small"
            sx={{
              bgcolor: alpha(theme.palette.success.main, 0.1),
              color: 'success.main',
              fontWeight: 600,
            }}
          />
        );
      case "Failed":
        return (
          <Chip 
            icon={<AlertCircleIcon fontSize="small" />} 
            label="Failed" 
            color="error" 
            size="small"
            sx={{
              bgcolor: alpha(theme.palette.error.main, 0.1),
              color: 'error.main',
              fontWeight: 600,
            }}
          />
        );
      default:
        return (
          <Chip 
            label={status} 
            size="small"
            sx={{
              bgcolor: alpha(theme.palette.grey[500], 0.1),
              color: 'text.secondary',
              fontWeight: 600,
            }}
          />
        );
    }
  };

  const formatAda = (lovelace) => {
  return `${(lovelace / 1_000_000).toFixed(2)} ADA`;
};

  const progressPercentage = nftsToMint > 0 ? (nftsMinted / nftsToMint) * 100 : 0;

  return (
    <Card 
      sx={{ 
        width: "100%", 
        bgcolor: "background.paper",
        borderRadius: 3,
        boxShadow: theme.palette.mode === 'dark' 
          ? '0 4px 6px -1px rgba(0, 0, 0, 0.3), 0 2px 4px -1px rgba(0, 0, 0, 0.2)' 
          : '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
        position: 'relative',
        overflow: 'hidden',
        '&::before': {
          content: '""',
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          height: 4,
          background: `linear-gradient(90deg, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`,
        }
      }}
    >
      <CardHeader
        title={
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Box sx={{ 
              width: 48, 
              height: 48, 
              borderRadius: 2, 
              bgcolor: alpha(theme.palette.primary.main, 0.1),
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'center'
            }}>
              <TrendingUpIcon sx={{ color: 'primary.main', fontSize: 24 }} />
            </Box>
            <Box>
              <Typography variant="h5" sx={{ fontWeight: 'bold', color: 'text.primary' }}>
                Received transactions
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
                Transaction activity in real time
              </Typography>
            </Box>
          </Box>
        }
        subheader={
          <Box sx={{
            display: "flex",
            alignItems: "center",
            flexWrap: "wrap",
            gap: 1,
            minWidth: 0,
            mt: 2,
            mb: 2,
            px: 1,
          }}>
            <Paper 
              elevation={0}
              sx={{ 
                px: 3, 
                py: 1, 
                bgcolor: alpha(theme.palette.primary.main, 0.1),
                border: `1px solid ${alpha(theme.palette.primary.main, 0.2)}`,
                borderRadius: 2,
                display: 'flex',
                alignItems: 'center',
                gap: 1
              }}
            >
              <WalletIcon sx={{ fontSize: 16, color: 'primary.main' }} />
              <Typography variant="caption" sx={{ fontWeight: 600, color: 'primary.main' }}>
                Monitoring:
              </Typography>
              <Tooltip title={monitoringAddress}>
                <Typography 
                  variant="caption" 
                  sx={{
                    fontFamily: 'monospace',
                    fontWeight: 600,
                    color: 'primary.main',
                    cursor: 'pointer'
                  }}
                >
                  {monitoringAddress.slice(0, 8)}...{monitoringAddress.slice(-6)}
                </Typography>
              </Tooltip>
            </Paper>
          </Box>
        }
        action={
          <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
            <Paper 
              elevation={0}
              sx={{ 
                display: 'flex', 
                alignItems: 'center', 
                gap: 1, 
                px: 2, 
                py: 1, 
                bgcolor: alpha(theme.palette.background.paper, 0.7),
                border: `1px solid ${alpha(theme.palette.divider, 0.2)}`,
                borderRadius: 2
              }}
            >
              <Box sx={{ 
                width: 8, 
                height: 8, 
                borderRadius: '50%', 
                bgcolor: autoRefresh ? 'success.main' : 'grey.400',
                animation: autoRefresh ? 'pulse 2s infinite' : 'none'
              }} />
              <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 600 }}>
                Updated: {lastUpdated.toLocaleTimeString()}
              </Typography>
            </Paper>
            <Button 
              variant={autoRefresh ? "contained" : "outlined"}
              size="medium" 
              onClick={() => setAutoRefresh(!autoRefresh)}
              sx={{ 
                borderRadius: 2,
                px: 3,
                fontWeight: 600,
                textTransform: 'none',
                bgcolor: autoRefresh ? 'success.main' : 'transparent',
                color: autoRefresh ? 'white' : 'success.main',
                '&:hover': {
                  bgcolor: autoRefresh ? 'success.dark' : alpha(theme.palette.success.main, 0.1),
                }
              }}
            >
              {autoRefresh ? "Auto-refresh On" : "Auto-refresh Off"}
            </Button>
            <Button
              variant="outlined"
              size="medium"
              onClick={fetchTransactions}
              disabled={isLoading}
              sx={{ 
                minWidth: 48,
                width: 48,
                height: 48,
                borderRadius: 2,
                bgcolor: alpha(theme.palette.primary.main, 0.1),
                borderColor: alpha(theme.palette.primary.main, 0.2),
                '&:hover': {
                  bgcolor: alpha(theme.palette.primary.main, 0.2),
                }
              }}
            >
              {isLoading ? (
                <LoaderIcon 
                  fontSize="small" 
                  sx={{ 
                    color: 'primary.main',
                    animation: 'spin 1s linear infinite',
                    '@keyframes spin': {
                      '0%': {
                        transform: 'rotate(0deg)',
                      },
                      '100%': {
                        transform: 'rotate(360deg)',
                      },
                    },
                  }} 
                />
              ) : (
                <RefreshIcon fontSize="small" sx={{ color: 'primary.main' }} />
              )}
            </Button>
          </Box>
        }
        sx={{ pb: 0 }}
      />

      {/* NFT Stats Section */}
      <Box sx={{ px: 3, pb: 3}}>
        <Paper 
          elevation={0} 
          sx={{ 
            p: 3, 
            bgcolor: alpha(theme.palette.background.paper, 0.7),
            border: `1px solid ${alpha(theme.palette.divider, 0.1)}`,
            borderRadius: 3,
            position: 'relative',
            overflow: 'hidden'
          }}
        >
          <Grid container spacing={3} alignItems="center" justifyContent="center">
            <Grid item xs={12} sm={4}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Box sx={{ 
                  width: 48, 
                  height: 48, 
                  borderRadius: 2, 
                  bgcolor: alpha(theme.palette.info.main, 0.1),
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'center'
                }}>
                  <TokenIcon sx={{ color: 'info.main', fontSize: 24 }} />
                </Box>
                <Box>
                  <Typography variant="h4" sx={{ fontWeight: 'bold', color: 'text.primary' }}>
                    {nftsToMint}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 600 }}>
                    Total Collection
                  </Typography>
                </Box>
              </Box>
            </Grid>
            
            <Grid item xs={12} sm={4}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Box sx={{ 
                  width: 48, 
                  height: 48, 
                  borderRadius: 2, 
                  bgcolor: alpha(theme.palette.success.main, 0.1),
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'center'
                }}>
                  <CheckCircleIcon sx={{ color: 'success.main', fontSize: 24 }} />
                </Box>
                <Box>
                  <Typography variant="h4" sx={{ fontWeight: 'bold', color: 'text.primary' }}>
                    {nftsMinted}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 600 }}>
                    Minted ({progressPercentage.toFixed(1)}%)
                  </Typography>
                </Box>
              </Box>
            </Grid>
            
            <Grid item xs={12} sm={4}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Box sx={{ 
                  width: 48, 
                  height: 48, 
                  borderRadius: 2, 
                  bgcolor: alpha(theme.palette.warning.main, 0.1),
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'center'
                }}>
                  <BalanceIcon sx={{ color: 'warning.main', fontSize: 24 }} />
                </Box>
                <Box>
                  <Typography variant="h4" sx={{ fontWeight: 'bold', color: 'text.primary' }}>
                    {adaPrice}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 600 }}>
                    ADA per NFT
                  </Typography>
                </Box>
              </Box>
            </Grid>
          </Grid>
          
          <Box sx={{ mt: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
              <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 600 }}>
                Minting Progress
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 600 }}>
                {nftsMinted} / {nftsToMint}
              </Typography>
            </Box>
            <LinearProgress
              variant="determinate"
              value={progressPercentage}
              sx={{
                height: 8,
                borderRadius: 4,
                bgcolor: alpha(theme.palette.success.main, 0.1),
                '& .MuiLinearProgress-bar': {
                  borderRadius: 4,
                  background: `linear-gradient(90deg, ${theme.palette.success.main}, ${theme.palette.success.light})`,
                },
              }}
            />
          </Box>
        </Paper>
      </Box>

      <CardContent sx={{ pt: 0 }}>
        {error && (
          <Alert 
            severity="error" 
            sx={{ 
              mb: 3,
              borderRadius: 2,
              bgcolor: alpha(theme.palette.error.main, 0.1),
              color: 'error.main',
              border: `1px solid ${alpha(theme.palette.error.main, 0.2)}`,
              '& .MuiAlert-icon': {
                color: 'error.main'
              }
            }}
          >
            {error}
          </Alert>
        )}

        {isLoading && (
          <Box sx={{ mb: 3 }}>
            <LinearProgress 
              sx={{
                height: 4,
                borderRadius: 2,
                bgcolor: alpha(theme.palette.primary.main, 0.1),
                '& .MuiLinearProgress-bar': {
                  borderRadius: 2,
                  background: `linear-gradient(90deg, ${theme.palette.primary.main}, ${theme.palette.primary.light})`,
                },
              }}
            />
          </Box>
        )}

        <TableContainer 
          component={Paper} 
          variant="outlined"
          sx={{
            borderRadius: 3,
            border: `1px solid ${alpha(theme.palette.divider, 0.1)}`,
            bgcolor: alpha(theme.palette.background.paper, 0.7),
            backdropFilter: 'blur(10px)',
            overflow: 'hidden'
          }}
        >
          <Table size="small">
            <TableHead>
              <TableRow sx={{ bgcolor: alpha(theme.palette.primary.main, 0.05) }}>
                <TableCell sx={{ fontWeight: 'bold', color: 'text.primary', py: 2 }}>Sender Address</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: 'text.primary', py: 2 }}>Amount Sent</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: 'text.primary', py: 2 }}>Timestamp</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: 'text.primary', py: 2 }}>Status</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: 'text.primary', py: 2 }}>Refund</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: 'text.primary', py: 2 }}>NFTs Minted</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: 'text.primary', py: 2 }}>NFTs To Mint</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: 'text.primary', py: 2 }}>Transaction Hash</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {transactions.length > 0 ? (
                transactions.map((tx, index) => (
                  <TableRow 
                    key={tx.id}
                    sx={{
                      '&:nth-of-type(odd)': {
                        bgcolor: alpha(theme.palette.action.hover, 0.02),
                      },
                      '&:hover': {
                        bgcolor: alpha(theme.palette.action.hover, 0.05),
                      },
                      transition: 'background-color 0.2s ease'
                    }}
                  >
                    <TableCell sx={{ py: 2 }}>
                      <Paper
                        elevation={0}
                        sx={{
                          px: 2,
                          py: 1,
                          bgcolor: alpha(theme.palette.primary.main, 0.05),
                          border: `1px solid ${alpha(theme.palette.primary.main, 0.1)}`,
                          borderRadius: 2,
                          display: 'inline-block'
                        }}
                      >
                        <Tooltip title={tx.senderAddress}>
                          <Typography 
                            variant="caption" 
                            sx={{ 
                              fontFamily: "monospace", 
                              fontSize: "0.75rem",
                              fontWeight: 600,
                              color: 'primary.main'
                            }}
                          >
                            {tx.senderAddress.slice(0, 8)}...{tx.senderAddress.slice(-6)}
                          </Typography>
                        </Tooltip>
                      </Paper>
                    </TableCell>

                    <TableCell sx={{ py: 2 }}>
                      <Typography variant="body2" sx={{ fontWeight: 600, color: 'text.primary' }}>
                        {formatAda(tx.amount)}
                      </Typography>
                    </TableCell>
                    
                    <TableCell sx={{ py: 2 }}>
                      <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
                        {formatTimestamp(tx.blockTime)}
                      </Typography>
                    </TableCell>
                    
                    <TableCell sx={{ py: 2 }}>{getStatusBadge(tx.status)}</TableCell>
                    
                    <TableCell sx={{ py: 2 }}>
                      {tx.refund > 0 ? (
                        <Tooltip title={tx.refunded === true ? "Refunded" : "Not refunded"}>
                          <Chip
                            label={formatAda(tx.refund)}
                            color={tx.refunded === true ? "success" : "error"}
                            size="small"
                            variant="filled"
                            sx={{
                              fontWeight: 600,
                              bgcolor: tx.refunded === true 
                                ? alpha(theme.palette.success.main, 0.1)
                                : alpha(theme.palette.error.main, 0.1),
                              color: tx.refunded === true ? 'success.main' : 'error.main',
                            }}
                          />
                        </Tooltip>
                      ) : (
                        <Typography variant="body2" color="text.disabled" sx={{ fontStyle: 'italic' }}>
                          No refund
                        </Typography>
                      )}
                    </TableCell>

                    <TableCell sx={{ py: 2 }}>
                      <Typography variant="body2" sx={{ fontWeight: 600, color: 'text.primary' }}>
                        {tx.amountMinted}
                      </Typography>
                    </TableCell>
                    
                    <TableCell sx={{ py: 2 }}>
                      <Typography variant="body2" sx={{ fontWeight: 600, color: 'text.primary' }}>
                        {tx.amountToMint}
                      </Typography>
                    </TableCell>
                    
                    <TableCell sx={{ py: 2 }}>
                      <Paper
                        elevation={0}
                        sx={{
                          px: 2,
                          py: 1,
                          bgcolor: alpha(theme.palette.secondary.main, 0.05),
                          border: `1px solid ${alpha(theme.palette.secondary.main, 0.1)}`,
                          borderRadius: 2,
                          display: 'inline-block'
                        }}
                      >
                        <Tooltip title={tx.txHash}>
                          <Typography 
                            variant="caption" 
                            sx={{ 
                              fontFamily: "monospace", 
                              fontSize: "0.75rem",
                              fontWeight: 600,
                              color: 'secondary.main'
                            }}
                          >
                            {tx.txHash.slice(0, 6)}...{tx.txHash.slice(-4)}
                          </Typography>
                        </Tooltip>
                      </Paper>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 6 }}>
                    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
                      <Box sx={{ 
                        width: 64, 
                        height: 64, 
                        borderRadius: '50%', 
                        bgcolor: alpha(theme.palette.grey[500], 0.1),
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center'
                      }}>
                        <TrendingUpIcon sx={{ fontSize: 32, color: 'text.disabled' }} />
                      </Box>
                      <Typography variant="h6" color="text.secondary" sx={{ fontWeight: 600 }}>
                        No transactions found
                      </Typography>
                      <Typography variant="body2" color="text.disabled">
                        Transactions will appear here once vending machine receives payments
                      </Typography>
                    </Box>
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