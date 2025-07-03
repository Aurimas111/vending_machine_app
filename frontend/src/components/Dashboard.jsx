import React, { useState } from "react";
import { Tabs, Tab, Box, Card, Typography } from "@mui/material";
import ConfigurationPanel from "./ConfigPanel/ConfigurationPanel";
import TransactionMonitor from "./TransactionMonitor";
import MintingDashboard from "./MintingDashboard";

const Dashboard = ({ className = "", walletAddress, activeTab, setActiveTab }) => {

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

  const TabPanel = ({ children, value, index, ...other }) => {
    return (
      <div
        role="tabpanel"
        hidden={value !== index}
        id={`tabpanel-${index}`}
        aria-labelledby={`tab-${index}`}
        {...other}
      >
        {value === index && <Box sx={{ mt: 2 }}>{children}</Box>}
      </div>
    );
  };

  return (
    <Box
      sx={{
        width: "100%",
        height: "100%",
        bgcolor: "background.default",
        p: 2,
        ...className,
      }}
    >
      <Card
        sx={{
          width: "100%",
          height: "100%",
          overflow: "hidden",
          boxShadow: 1,
          borderRadius: 2,
        }}
      >
        <Box sx={{ p: 3 }}>
          <Typography
            variant="h4"
            component="h2"
            sx={{ mb: 3, fontWeight: "bold" }}
          >
            NFT Vending Machine Dashboard
          </Typography>

          <Box sx={{ width: "100%" }}>
            <Tabs
              value={activeTab}
              onChange={handleTabChange}
              sx={{ mb: 4 }}
              variant="fullWidth"
            >
              <Tab label="Configuration" value="configuration" />
              <Tab label="Transaction Monitoring" value="transactions" />
              <Tab label="Collection Status" value="minting" />
            </Tabs>

            <TabPanel value={activeTab} index="configuration">
              <ConfigurationPanel walletAddress={walletAddress}/>
            </TabPanel>

            <TabPanel value={activeTab} index="transactions">
              <TransactionMonitor walletAddress={walletAddress}/>
            </TabPanel>

            <TabPanel value={activeTab} index="minting">
              <MintingDashboard walletAddress={walletAddress}/>
            </TabPanel>
          </Box>
        </Box>
      </Card>
    </Box>
  );
};

export default React.memo(Dashboard);
