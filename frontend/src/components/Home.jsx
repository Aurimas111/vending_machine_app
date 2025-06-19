import React, { useEffect, useState } from "react";
import {
  DarkMode as MoonIcon,
  LightMode as SunIcon,
  Wallet as WalletIcon,
  Logout as LogoutIcon,
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
} from "@mui/material";

import Dashboard from "./Dashboard";
import mintsService from "../services/mintsService";
import * as Cardano from "@emurgo/cardano-serialization-lib-browser";
import refundsService from "../services/refundsService";

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
      if (!window.cardano || !window.cardano.eternl) {
        alert("Eternl Wallet not found. Please install it.");
        return;
      }

      const api = await window.cardano.eternl.enable();
      const usedAddresses = await api.getUsedAddresses(); // Hex format

      if (usedAddresses.length === 0) {
        alert("No used addresses found in wallet.");
        return;
      }

      // Convert to Bech32 address
      const raw = usedAddresses[0];
      function hexToBytes(hex) {
      return Uint8Array.from(hex.match(/.{1,2}/g).map(byte => parseInt(byte, 16)));
      }

      const address = Cardano.Address.from_bytes(hexToBytes(raw)).to_bech32();

      setWalletAddress(address);
      setIsWalletConnected(true);
      localStorage.setItem("wallet_connected", "true");
    } catch (err) {
      console.error("Wallet connection failed", err);
      alert("Failed to connect to Eternl wallet.");
    }
  };

  const disconnectWallet = () => {
    setWalletAddress("");
    setIsWalletConnected(false);
    localStorage.removeItem("wallet_connected");
  };

  useEffect(() => {
  let tries = 0;
  const maxTries = 50; // Try for up to 5 seconds (50 * 100ms)
  
  function tryConnect() {
    if (localStorage.getItem("wallet_connected") === "true") {
      if (window.cardano && window.cardano.eternl) {
        connectWallet();
      } else if (tries < maxTries) {
        tries++;
        setTimeout(tryConnect, 100);
      }
    }
  }
  tryConnect();
  }, []);


  const theme = createTheme({
    palette: {
      mode: isDarkMode ? "dark" : "light",
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
      await mintsService.startMint({ walletAddress });
    } else {
      await mintsService.stopMint({ walletAddress });
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
    const res = await refundsService.startRefunds({ walletAddress });
  } else {
    const res = await refundsService.stopRefunds({ walletAddress });
  }
  } finally {
    setTimeout(() => setSwitchDisabled(false), 5000); // don't allow toggling for 5 seconds
  }
};

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box sx={{ minHeight: "100vh", display: "flex", flexDirection: "column" }}>
        <AppBar position="sticky" color="default" elevation={1}>
          <Toolbar sx={{ px: 2 }}>
            <Box sx={{ flexGrow: 1 }}>
              <Typography variant="h6" sx={{ fontWeight: "bold" }}>
                NFT Vending Machine
              </Typography>
            </Box>

          <Switch
            checked={isVendingMachineActive}
            onChange={(e) => handleVendingToggle(e.target.checked)}
            color="primary"
            disabled={switchDisabled || refundActive}
          />
          <Typography
            variant="body2"
            sx={{ fontWeight: "bold", color: isVendingMachineActive ? "green" : "red", ml: 1 }}
          >
            {isVendingMachineActive ? "Vending Enabled" : "Vending Disabled"}
          </Typography>

          <Switch
            checked={refundActive}
            onChange={(e) => handleRefundToggle(e.target.checked)}
            color="primary"
            disabled={switchDisabled || isVendingMachineActive}
          />
          <Typography
            variant="body2"
            sx={{ fontWeight: "bold", color: refundActive ? "green" : "red", ml: 1 }}
          >
            {refundActive ? "Refunds Enabled" : "Refunds Disabled"}
          </Typography>

                      <Button
              variant="text"
              color="inherit"
              onClick={() => console.log("Help clicked")}
              sx={{ ml: 2 }}
              ></Button>

            {isWalletConnected && (
              <>
                <Typography variant="body2" sx={{ fontSize: "0.8rem", maxWidth: 150, overflow: "hidden", textOverflow: "ellipsis" }}>
                  {walletAddress}
                </Typography>
              </>
            )}

            <Button
              variant={isWalletConnected ? "outlined" : "contained"}
              onClick={isWalletConnected ? disconnectWallet : connectWallet}
              startIcon={isWalletConnected ? <LogoutIcon /> : <WalletIcon />}
            >
              {isWalletConnected ? "Disconnect" : "Connect Wallet"}
            </Button>
            <IconButton onClick={toggleTheme} color="inherit">
              {isDarkMode ? <SunIcon /> : <MoonIcon />}
            </IconButton>
          </Toolbar>
        </AppBar>

        <Container sx={{ flex: 1, display: "flex", justifyContent: "center", alignItems: "center", py: 3 }}>
          <Grid container spacing={3} sx={{ justifyContent: "center", alignItems: "center" }}>
            <Grid item xs={12} sm={8} md={6} lg={4}>
              {!isWalletConnected ? (
                <Card sx={{ display: "flex", flexDirection: "column", alignItems: "center", p: { xs: 4, sm: 6 }, textAlign: "center" }}>
                  <WalletIcon sx={{ fontSize: 48, mb: 2, color: "text.secondary" }} />
                  <Typography variant="h5" component="h2" sx={{ mb: 1, fontWeight: "bold" }}>
                    Connect Your Wallet
                  </Typography>
                  <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                    Connect your Cardano wallet to configure and monitor the NFT vending machine.
                  </Typography>
                  <Button variant="contained" onClick={connectWallet} startIcon={<WalletIcon />}>
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

        <Box component="footer" sx={{ borderTop: 1, borderColor: "divider", bgcolor: "background.paper", mt: "auto" }}>
          <Container sx={{ display: "flex", height: 64, alignItems: "center", justifyContent: "space-between" }}>
            <Typography variant="body2" color="text.secondary">Powered by Cardano Blockchain</Typography>
          </Container>
        </Box>
      </Box>
    </ThemeProvider>
  );
};

export default Home;