import React, { useEffect, useState } from "react";
import {
  Card,
  CardContent,
  CardActions,
  CardHeader,
  Typography,
  Tabs,
  Tab,
  Box,
  TextField,
  Button,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Chip,
  Avatar,
  Grid,
  Paper,
  InputAdornment,
  Tooltip,
} from "@mui/material";
import {
  Search as SearchIcon,
  FilterList as FilterIcon,
  SwapVert as ArrowUpDownIcon,
} from "@mui/icons-material";
import mintsService from "../services/mintsService";

const MintingDashboard = ({walletAddress}) => {
  const [filter, setFilter] = useState("all");
  const [sortBy, setSortBy] = useState("newest");
  const [searchQuery, setSearchQuery] = useState("");
  const [nfts, setNfts] = useState([]);
  const [loading, setLoading] = useState(true);


  // Filter and sort the mints
  const filteredMints = nfts
    .filter((mint) => {
      if (filter === "all") return true;
      if (filter === "minted") return mint.status === "minted";
      if (filter === "not minted") return mint.status === "not minted";
      return true;
    })
    .filter((mint) => {
      if (!searchQuery) return true;
      return (
        mint.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        mint.txHash.toLowerCase().includes(searchQuery.toLowerCase()) ||
        mint.recipient.toLowerCase().includes(searchQuery.toLowerCase())
      );
    })
    .sort((a, b) => {
      if (sortBy === "newest") {
        return (
          new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
        );
      } else if (sortBy === "oldest") {
        return (
          new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime()
        );
      } else if (sortBy === "name") {
        return a.name.localeCompare(b.name);
      }
      return 0;
    });

  // Format date for display
  const formatDate = (dateValue) => {
    if (!dateValue || dateValue === "N/A") return "N/A";
    const timestamp = Number(dateValue);
    if (isNaN(timestamp)) return "N/A";
    const date = new Date(
      timestamp > 1e12 ? timestamp : timestamp * 1000
    );
    if (isNaN(date.getTime())) return "N/A";
    return date.toLocaleDateString() + " " + date.toLocaleTimeString();
  };

  // Truncate long strings
  const truncate = (str, n) => {
    return str.length > n ? str.substr(0, n - 1) + "..." : str;
  };

  const [tabValue, setTabValue] = useState("grid");

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  useEffect(() => {
    let didFetch = false;
    const fetchOnce = async () => {
      if (didFetch) return;
      didFetch = true;
      setLoading(true);
      try {
        const response = await mintsService.getMints({ walletAddress });
        const fetchedNfts = response.data.map((item, index) => ({
          id: index.toString(),
          name: item.name,
          image: `https://ipfs.io/ipfs/${item.image}`,
          txHash: item.txHash || "N/A",
          timestamp: item.timeStamp || "N/A",
          recipient: item.receiverAddress || "N/A",
          status: item.minted ? "Minted" : "Not minted",
          metadata: item.dynamicAttributes || null,
        }));
        setNfts(fetchedNfts);
      } catch (error) {
        console.error("Error fetching mints:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchOnce();
  }, [walletAddress]);


  return (
    <Box
      sx={{
        bgcolor: "background.default",
        width: "100%",
        height: "100%",
        p: 3,
      }}
    >
      <Card sx={{ width: "100%" }}>
        <CardHeader
          title="Minting Dashboard"
          subheader="View all NFTs from your vending machine."
        />
        <CardContent>
          <Box sx={{ display: "flex", flexDirection: "column", gap: 3 }}>
            <Box
              sx={{
                display: "flex",
                flexDirection: { xs: "column", md: "row" },
                justifyContent: "space-between",
                gap: 2,
              }}
            >
              <TextField
                placeholder="Search by name, tx hash, or recipient"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                sx={{ width: { xs: "100%", md: "33%" } }}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon fontSize="small" />
                    </InputAdornment>
                  ),
                }}
              />
              <Box
                sx={{
                  display: "flex",
                  flexDirection: { xs: "column", sm: "row" },
                  gap: 2,
                }}
              >
                <FormControl sx={{ minWidth: 180 }}>
                  <InputLabel id="filter-label">Filter by status</InputLabel>
                  <Select
                    labelId="filter-label"
                    value={filter}
                    onChange={(e) => setFilter(e.target.value)}
                    startAdornment={
                      <FilterIcon fontSize="small" sx={{ mr: 1 }} />
                    }
                    label="Filter by status"
                  >
                    <MenuItem value="all">All NFTs</MenuItem>
                    <MenuItem value="minted">Minted</MenuItem>
                    <MenuItem value="not minted">Not minted</MenuItem>
                  </Select>
                </FormControl>
                <FormControl sx={{ minWidth: 180 }}>
                  <InputLabel id="sort-label">Sort by</InputLabel>
                  <Select
                    labelId="sort-label"
                    value={sortBy}
                    onChange={(e) => setSortBy(e.target.value)}
                    startAdornment={
                      <ArrowUpDownIcon fontSize="small" sx={{ mr: 1 }} />
                    }
                    label="Sort by"
                  >
                    <MenuItem value="newest">Newest First</MenuItem>
                    <MenuItem value="oldest">Oldest First</MenuItem>
                    <MenuItem value="name">Name (A-Z)</MenuItem>
                  </Select>
                </FormControl>
              </Box>
            </Box>

            <Box sx={{ width: "100%" }}>
              <Tabs value={tabValue} onChange={handleTabChange} sx={{ mb: 2 }}>
                <Tab label="Grid View" value="grid" />
                <Tab label="List View" value="list" />
              </Tabs>

              
            {loading ? (
              <Box sx={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: 300 }}>
                <Typography variant="body2" color="text.secondary">
                  Loading NFTs...
                </Typography>
              </Box>
            ) : (
              <>
              {tabValue === "grid" && (
                  <Grid container spacing={5} justifyContent="center">
                    {filteredMints.map((mint) => (
                      <Grid item xs={12} sm={6} md={3} lg={3} key={mint.id}>
                        
                      <Card
                        sx={{
                          height: "100%",
                          display: "flex",
                          flexDirection: "column",
                        }}
                      >
                        <Box sx={{ position: "relative" }}>
                          <Box
                            component="img"
                            src={mint.image}
                            alt={mint.name}
                            sx={{
                              width: "100%",
                              height: 220,
                              aspectRatio: "1/1",
                              objectFit: "cover",
                              bgcolor: "action.hover",
                            }}
                          />
                          <Chip
                            label={mint.status}
                            color={mint.status === "Minted" ? "success" : "error"}
                            size="small"
                            sx={{ position: "absolute", top: 8, right: 8 }}
                          />
                        </Box>
                        <CardHeader title={mint.name} sx={{ pb: 0 }} />
                        <CardContent sx={{ pt: 1, pb: 1, flexGrow: 1 }}>
                          <Typography
                            variant="caption"
                            color="text.secondary"
                            component="div"
                          >
                            <Box
                              sx={{ fontWeight: "medium", display: "inline" }}
                            >
                              Tx Hash:
                            </Box>{" "}
                            <Tooltip title={mint.txHash}>
                            <span>{truncate(mint.txHash, 20)}</span>
                            </Tooltip>
                          </Typography>
                          <Typography
                            variant="caption"
                            color="text.secondary"
                            component="div"
                          >
                            <Box
                              sx={{ fontWeight: "medium", display: "inline" }}
                            >
                              Recipient:
                            </Box>{" "}
                            <Tooltip title={mint.recipient}>
                            <span>{truncate(mint.recipient, 20)}</span>
                            </Tooltip>
                          </Typography>
                          <Typography
                            variant="caption"
                            color="text.secondary"
                            component="div"
                          >
                            <Box
                              sx={{ fontWeight: "medium", display: "inline" }}
                            >
                              Date:
                            </Box>{" "}
                            {formatDate(mint.timestamp)}
                          </Typography>
                        </CardContent>
                        <CardActions sx={{ p: 2, pt: 0 }}>
                        <Tooltip
                          title={
                            <Box sx={{ maxWidth: 350, whiteSpace: "pre-wrap", fontSize: 13 }}>
                              {mint.metadata && typeof mint.metadata === "object" ? (
                                <Box component="ul" sx={{ pl: 2, mb: 0 }}>
                                  {Object.entries(mint.metadata).map(([key, value]) => (
                                    <li key={key}>
                                      <strong>{key}:</strong> {String(value)}
                                    </li>
                                  ))}
                                </Box>
                              ) : (
                                "No metadata available"
                              )}
                            </Box>
                          }
                          arrow
                          placement="top"
                        >
                          <Button variant="outlined" size="small" fullWidth>
                            View Metadata
                          </Button>
                        </Tooltip>
                        </CardActions>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              )}

              {tabValue === "list" && (
                <Paper variant="outlined">
                  <Box
                    sx={{
                      display: "grid",
                      gridTemplateColumns: {
                        xs: "1fr 2fr 2fr 1fr",
                        md: "80px 2fr 2fr 2fr 1fr",
                      },
                      gap: 2,
                      p: 2,
                      fontWeight: "medium",
                      borderBottom: 1,
                      borderColor: "divider",
                    }}
                  >
                    <Box sx={{ display: { xs: "none", md: "block" } }}>
                      Image
                    </Box>
                    <Box>Name</Box>
                    <Box>Transaction</Box>
                    <Box sx={{ display: { xs: "none", md: "block" } }}>
                      Recipient
                    </Box>
                    <Box>Status</Box>
                  </Box>
                  {filteredMints.map((mint) => (
                    <Box
                      key={mint.id}
                      sx={{
                        display: "grid",
                        gridTemplateColumns: {
                          xs: "1fr 2fr 2fr 1fr",
                          md: "80px 2fr 2fr 2fr 1fr",
                        },
                        gap: 2,
                        p: 2,
                        alignItems: "center",
                        borderBottom: 1,
                        borderColor: "divider",
                        "&:hover": { bgcolor: "action.hover" },
                      }}
                    >
                      <Box sx={{ display: { xs: "none", md: "block" } }}>
                        <Avatar src={mint.image} alt={mint.name}>
                          {mint.name.substring(0, 2)}
                        </Avatar>
                      </Box>
                      <Typography sx={{ fontWeight: "medium" }}>
                        {mint.name}
                      </Typography>
                      <Box>
                        <Typography variant="body2" noWrap>
                            <Tooltip title={mint.txHash}>
                            <span>{truncate(mint.txHash, 16)}</span>
                            </Tooltip>
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {formatDate(mint.timestamp)}
                        </Typography>
                      </Box>
                      <Typography
                        variant="body2"
                        noWrap
                        sx={{ display: { xs: "none", md: "block" } }}
                      >
 
                      <Tooltip title={mint.recipient}>
                      <span>{truncate(mint.recipient, 25)}</span>
                      </Tooltip>
                      </Typography>
                      <Box>
                      <Chip
                        label={mint.status}
                        color={mint.status === "Minted" ? "success" : "error"}
                        size="small"
                      />
                      </Box>
                    </Box>
                  ))}
                </Paper>
              )}
              </>
            )}
            </Box>
          </Box>
        </CardContent>
        <CardActions sx={{ justifyContent: "space-between", p: 2 }}>
          <Typography variant="body2" color="text.secondary">
            Showing {filteredMints.length} of {nfts.length} NFTs
          </Typography>
        </CardActions>
      </Card>
    </Box>
  );
};

export default MintingDashboard;
