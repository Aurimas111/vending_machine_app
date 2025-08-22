import React, { useEffect, useState } from "react";
import {
  DarkMode as MoonIcon,
  LightMode as SunIcon,
  Wallet as WalletIcon,
  Logout as LogoutIcon,
  PowerSettingsNew as PowerIcon,
  RefreshOutlined as RefreshIcon,
} from "@mui/icons-material";
import {
  Button,
  Box,
  Card,
  Typography,
  ThemeProvider,
  createTheme,
  CssBaseline,
  IconButton,
  AppBar,
  Toolbar,
  Container,
  Grid,
  Switch,
  Chip,
  Paper,
  alpha,
} from "@mui/material";

import Dashboard from "./Dashboard";
import mintsService from "../services/mintsService";
import refundsService from "../services/refundsService";
import walletService from "../services/walletService";

const Home = () => {
  const [activeTab, setActiveTab] = useState("configuration");

  const [isDarkMode, setIsDarkMode] = useState(false);
  const [isWalletConnected, setIsWalletConnected] = useState(false);
  const [walletAddress, setWalletAddress] = useState("");
  const [isVendingMachineActive, setIsVendingMachineActive] = useState(false);
  const [refundActive, setRefundActive] = useState(false);
  const [switchDisabled, setSwitchDisabled] = useState(false);

  const toggleTheme = () => {
    setIsDarkMode(!isDarkMode);
    document.documentElement.classList.toggle("dark");
  };

const connectWallet = async () => {
    try {
      const { address } = await walletService.connectWallet();
      setWalletAddress(address);
      setIsWalletConnected(true);
    } catch (err) {
      alert(`Failed to connect to Eternl wallet: ${err.message}`);
    }
  };

  const disconnectWallet = () => {
    walletService.disconnectWallet();
    setWalletAddress("");
    setIsWalletConnected(false);
  };

  useEffect(() => {
  let tries = 0;
  const maxTries = 50; // Try for up to 5 seconds (50 * 100ms)
  
  function tryConnect() {
     if (!walletService.isWalletConnected()) {
      if (walletService.isWalletAvailable()) {
        connectWallet();
      } else if (tries < maxTries) {
        tries++;
        setTimeout(tryConnect, 100);
      }
    }else{
      setWalletAddress(walletService.getStoredAddress());
      setIsWalletConnected(true);
    }
  }
  tryConnect();
  }, []);


  const theme = createTheme({
    palette: {
      mode: isDarkMode ? "dark" : "light",
      primary: {
        main: isDarkMode ? "#ffffff" : "#000000",
        light: isDarkMode ? "#f5f5f5" : "#333333",
        dark: isDarkMode ? "#e0e0e0" : "#000000",
      },
      secondary: {
        main: isDarkMode ? "#424242" : "#666666",
      },
      background: {
        default: isDarkMode ? "#0f172a" : "#f8fafc",
        paper: isDarkMode ? "#1e293b" : "#ffffff",
      },
      success: {
        main: "#10b981",
        light: "#34d399",
        dark: "#059669",
      },
      error: {
        main: "#ef4444",
        light: "#f87171",
        dark: "#dc2626",
      },
    },
    typography: {
      fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
      h6: {
        fontWeight: 600,
      },
    },
    components: {
      MuiButton: {
        styleOverrides: {
          root: {
            borderRadius: 12,
            textTransform: 'none',
            fontWeight: 600,
            padding: '10px 24px',
          },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            borderRadius: 16,
            boxShadow: isDarkMode 
              ? '0 4px 6px -1px rgba(0, 0, 0, 0.3), 0 2px 4px -1px rgba(0, 0, 0, 0.2)' 
              : '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
          },
        },
      },
      MuiAppBar: {
        styleOverrides: {
          root: {
            backdropFilter: 'blur(20px)',
            backgroundColor: isDarkMode 
              ? alpha('#1e293b', 0.8) 
              : alpha('#ffffff', 0.8),
            boxShadow: 'none',
            borderBottom: `1px solid ${isDarkMode ? '#334155' : '#e2e8f0'}`,
          },
        },
      },
    },
  });

  const handleVendingToggle = async (checked) => {
  setSwitchDisabled(true);
  setIsVendingMachineActive(checked);
  if(refundActive && checked) {
  setRefundActive(false); // Only allow one
  }

  try {
    if (checked) {
      await mintsService.startMint();
    } else {
      await mintsService.stopMint();
    }
  } finally {
    setTimeout(() => setSwitchDisabled(false), 10000);
  }
};

const handleRefundToggle = async (checked) => {
  setSwitchDisabled(true);
  setRefundActive(checked);
  if (checked && isVendingMachineActive) {
    setIsVendingMachineActive(false); // Disable vending if refund is toggled on
  }

  try {
  if (checked) {
    const res = await refundsService.startRefunds();
  } else {
    const res = await refundsService.stopRefunds();
  }
  } finally {
    setTimeout(() => setSwitchDisabled(false), 5000); // don't allow toggling for 5 seconds
  }
};

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box sx={{ minHeight: "100vh", display: "flex", flexDirection: "column", bgcolor: 'background.default' }}>
        <AppBar position="sticky" color="default" elevation={0}>
          <Toolbar sx={{ px: 3, py: 1 }}>
            <Box sx={{ flexGrow: 1, display: 'flex', alignItems: 'center' }}>
              <Box sx={{ 
                width: 40, 
                height: 40, 
                borderRadius: 2, 
                bgcolor: 'primary.main', 
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'center',
                mr: 2
              }}>
                <Typography variant="h6" sx={{ color: isDarkMode ? 'black' : 'white', fontWeight: 'bold' }}>
                  N
                </Typography>
              </Box>
              <Typography variant="h6" sx={{ fontWeight: "bold", color: 'text.primary' }}>
                NFT Vending Machine
              </Typography>
            </Box>

            {/* Status Controls */}
            <Paper 
              elevation={0} 
              sx={{ 
                display: 'flex', 
                alignItems: 'center', 
                gap: 3, 
                px: 3, 
                py: 1.5, 
                bgcolor: alpha(theme.palette.background.paper, 0.7),
                backdropFilter: 'blur(10px)',
                borderRadius: 3,
                border: `1px solid ${alpha(theme.palette.divider, 0.1)}`,
                mr: 2
              }}
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <PowerIcon sx={{ fontSize: 20, color: isVendingMachineActive ? 'success.main' : 'text.disabled' }} />
                <Switch
                  checked={isVendingMachineActive}
                  onChange={(e) => handleVendingToggle(e.target.checked)}
                  color="success"
                  disabled={!isWalletConnected || switchDisabled || refundActive}
                  size="small"
                />
                <Chip
                  label={isVendingMachineActive ? "Vending Active" : "Vending Inactive"}
                  size="small"
                  color={isVendingMachineActive ? "success" : "error"}
                  variant="outlined"
                  sx={{ fontWeight: 600, fontSize: '0.75rem' }}
                />
              </Box>

              <Box sx={{ width: 1, height: 24, bgcolor: 'divider' }} />

              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <RefreshIcon sx={{ fontSize: 20, color: refundActive ? 'success.main' : 'text.disabled' }} />
                <Switch
                  checked={refundActive}
                  onChange={(e) => handleRefundToggle(e.target.checked)}
                  color="success"
                  disabled={!isWalletConnected || switchDisabled || isVendingMachineActive}
                  size="small"
                />
                <Chip
                  label={refundActive ? "Refunds Active" : "Refunds Inactive"}
                  size="small"
                  color={refundActive ? "success" : "error"}
                  variant="outlined"
                  sx={{ fontWeight: 600, fontSize: '0.75rem' }}
                />
              </Box>
            </Paper>

            {/* Wallet Section */}
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              {isWalletConnected && (
                <Paper 
                  elevation={0}
                  sx={{ 
                    px: 2, 
                    py: 1, 
                    bgcolor: alpha(theme.palette.primary.main, 0.1),
                    border: `1px solid ${alpha(theme.palette.primary.main, 0.2)}`,
                    borderRadius: 2
                  }}
                >
                  <Typography 
                    variant="caption" 
                    sx={{ 
                      fontSize: "0.75rem", 
                      maxWidth: 120, 
                      overflow: "hidden", 
                      textOverflow: "ellipsis",
                      fontFamily: 'monospace',
                      color: 'primary.main',
                      fontWeight: 600
                    }}
                  >
                    {walletAddress ? (
                      <>
                        {walletAddress.slice(0, 8)}...{walletAddress.slice(-6)}
                      </>
                    ) : null}
                    
                  </Typography>
                </Paper>
              )}

              <Button
                variant={isWalletConnected ? "outlined" : "contained"}
                onClick={isWalletConnected ? disconnectWallet : connectWallet}
                startIcon={isWalletConnected ? <LogoutIcon /> : <WalletIcon />}
                sx={{ 
                  borderRadius: 3,
                  px: 3,
                  bgcolor: isWalletConnected ? 'transparent' : 'primary.main',
                  color: isWalletConnected ? 'primary.main' : (isDarkMode ? 'black' : 'white'),
                  '&:hover': {
                    bgcolor: isWalletConnected ? alpha(theme.palette.primary.main, 0.1) : 'primary.dark',
                  }
                }}
              >
                {isWalletConnected ? "Disconnect" : "Connect Wallet"}
              </Button>

              <IconButton 
                onClick={toggleTheme} 
                color="inherit"
                sx={{ 
                  bgcolor: alpha(theme.palette.background.paper, 0.7),
                  '&:hover': {
                    bgcolor: alpha(theme.palette.background.paper, 0.9),
                  }
                }}
              >
                {isDarkMode ? <SunIcon /> : <MoonIcon />}
              </IconButton>
            </Box>
          </Toolbar>
        </AppBar>

        <Container 
          maxWidth="lg" 
          sx={{ 
            flex: 1, 
            display: "flex", 
            justifyContent: "center", 
            alignItems: "center", 
            py: 4,
            px: { xs: 2, sm: 3 }
          }}
        >
          <Grid container spacing={4} sx={{ justifyContent: "center", alignItems: "center" }}>
            <Grid item xs={12} lg={10} xl={8}>
              {!isWalletConnected ? (
                <Card 
                  sx={{ 
                    display: "flex", 
                    flexDirection: "column", 
                    alignItems: "center", 
                    p: { xs: 6, sm: 8 }, 
                    textAlign: "center",
                    bgcolor: 'background.paper',
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
                  <Box sx={{ 
                    width: 80, 
                    height: 80, 
                    borderRadius: '50%', 
                    bgcolor: alpha(theme.palette.primary.main, 0.1),
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    mb: 3
                  }}>
                    <WalletIcon sx={{ fontSize: 40, color: "primary.main" }} />
                  </Box>
                  <Typography 
                    variant="h4" 
                    component="h2" 
                    sx={{ 
                      mb: 2, 
                      fontWeight: "bold",
                      background: `linear-gradient(45deg, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`,
                      backgroundClip: 'text',
                      WebkitBackgroundClip: 'text',
                      WebkitTextFillColor: 'transparent',
                      fontSize: { xs: '1.75rem', sm: '2.125rem' }
                    }}
                  >
                    Connect Your Wallet
                  </Typography>
                  <Typography 
                    variant="body1" 
                    color="text.secondary" 
                    sx={{ 
                      mb: 4, 
                      maxWidth: 400,
                      lineHeight: 1.6,
                      fontSize: '1.1rem'
                    }}
                  >
                    Connect your Cardano wallet to configure and monitor your NFT vending machine. 
                  </Typography>
                  <Button 
                    variant="contained" 
                    size="large"
                    onClick={connectWallet} 
                    startIcon={<WalletIcon />}
                    sx={{ 
                      px: 4, 
                      py: 1.5,
                      fontSize: '1rem',
                      background: `linear-gradient(45deg, ${theme.palette.primary.main}, ${theme.palette.primary.dark})`,
                      color: isDarkMode ? 'black' : 'white',
                      boxShadow: `0 4px 14px 0 ${alpha(theme.palette.primary.main, 0.4)}`,
                      '&:hover': {
                        background: `linear-gradient(45deg, ${theme.palette.primary.dark}, ${theme.palette.primary.main})`,
                        boxShadow: `0 6px 20px 0 ${alpha(theme.palette.primary.main, 0.6)}`,
                      }
                    }}
                  >
                    Connect Wallet
                  </Button>
                </Card>
              ) : (
                <Dashboard 
                  walletAddress={walletAddress} 
                  activeTab={activeTab}
                  setActiveTab={setActiveTab}
                />
              )}
            </Grid>
          </Grid>
        </Container>

        <Box 
          component="footer" 
          sx={{ 
            borderTop: `1px solid ${alpha(theme.palette.divider, 0.1)}`,
            bgcolor: alpha(theme.palette.background.paper, 0.7),
            backdropFilter: 'blur(20px)',
            mt: "auto"
          }}
        >
          <Container sx={{ display: "flex", height: 64, alignItems: "center", justifyContent: "space-between" }}>
            <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
              Powered by Cardano Blockchain
            </Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box sx={{ 
                width: 8, 
                height: 8, 
                borderRadius: '50%', 
                bgcolor: 'success.main',
                animation: 'pulse 2s infinite'
              }} />
              <Typography variant="caption" color="text.secondary">
                Network Active
              </Typography>
            </Box>
          </Container>
        </Box>
      </Box>
    </ThemeProvider>
  );
};

export default Home;