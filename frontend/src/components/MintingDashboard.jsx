import React, { useEffect, useState, useRef } from "react";
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
  Grid,
  Paper,
  InputAdornment,
  Tooltip,
  alpha,
  useTheme,
  CircularProgress,
} from "@mui/material";
import {
  Search as SearchIcon,
  FilterList as FilterIcon,
  SwapVert as ArrowUpDownIcon,
  ViewModule as GridViewIcon,
  ViewList as ListViewIcon,
  InfoOutlined as InfoIcon,
} from "@mui/icons-material";
import mintsService from "../services/mintsService";
import LazyImage from "../components/misc/LazyImage";


const MintingDashboard = ({walletAddress}) => {
  const theme = useTheme();
  const [filter, setFilter] = useState("all");
  const [sortBy, setSortBy] = useState("newest");
  const [searchQuery, setSearchQuery] = useState("");
  const [nfts, setNfts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tabValue, setTabValue] = useState("grid");


  // filter and sort the mints
  const filteredMints = nfts
    .filter((mint) => {
      if (filter === "all") return true;
      if (filter === "minted") return mint.status === "Minted";
      if (filter === "not minted") return mint.status === "Not minted";
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


  // format date for display
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

  // truncate long strings
  const truncate = (str, n) => {
    return str.length > n ? str.substr(0, n - 1) + "..." : str;
  };

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
        p: { xs: 2, sm: 3 },
      }}
    >
      <Card 
        sx={{ 
          width: "100%",
          borderRadius: 4,
          boxShadow: theme.palette.mode === 'dark' 
            ? '0 8px 32px rgba(0, 0, 0, 0.3)' 
            : '0 8px 32px rgba(0, 0, 0, 0.08)',
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
        <CardHeader
          title={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box sx={{ 
                width: 48, 
                height: 48, 
                borderRadius: 3, 
                bgcolor: alpha(theme.palette.primary.main, 0.1),
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'center'
              }}>
                <InfoIcon sx={{ color: 'primary.main', fontSize: 24 }} />
              </Box>
              <Box>
                <Typography 
                  variant="h5" 
                  component="h1" 
                  sx={{ 
                    fontWeight: 'bold',
                    background: `linear-gradient(45deg, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`,
                    backgroundClip: 'text',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent',
                  }}
                >
                  Collection status
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                  View all of the NFTs from your collection
                </Typography>
              </Box>
            </Box>
          }
          sx={{ pb: 1 }}
        />
        
        <CardContent sx={{ pt: 0 }}>
          <Box sx={{ display: "flex", flexDirection: "column", gap: 4 }}>
            {/* Controls Section */}
            <Paper 
              elevation={0} 
              sx={{ 
                p: 3, 
                bgcolor: alpha(theme.palette.background.paper, 0.5),
                border: `1px solid ${alpha(theme.palette.divider, 0.1)}`,
                borderRadius: 3,
                backdropFilter: 'blur(10px)'
              }}
            >
              <Box
                sx={{
                  display: "flex",
                  flexDirection: { xs: "column", lg: "row" },
                  justifyContent: "space-between",
                  alignItems: { xs: "stretch", lg: "center" },
                  gap: 3,
                }}
              >
                <TextField
                  placeholder="Search..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  sx={{ 
                    flex: 1,
                    maxWidth: { lg: 400 },
                    '& .MuiOutlinedInput-root': {
                      borderRadius: 3,
                      bgcolor: alpha(theme.palette.background.paper, 0.8),
                    }
                  }}
                  InputProps={{
                    startAdornment: (
                      <InputAdornment position="start">
                        <SearchIcon sx={{ color: 'text.secondary' }} />
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
                        <FilterIcon fontSize="small" sx={{ mr: 1, color: 'text.secondary' }} />
                      }
                      label="Filter by status"
                      sx={{ 
                        borderRadius: 3,
                        bgcolor: alpha(theme.palette.background.paper, 0.8),
                      }}
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
                        <ArrowUpDownIcon fontSize="small" sx={{ mr: 1, color: 'text.secondary' }} />
                      }
                      label="Sort by"
                      sx={{ 
                        borderRadius: 3,
                        bgcolor: alpha(theme.palette.background.paper, 0.8),
                      }}
                    >
                      <MenuItem value="newest">Newest First</MenuItem>
                      <MenuItem value="oldest">Oldest First</MenuItem>
                      <MenuItem value="name">Name (A-Z)</MenuItem>
                    </Select>
                  </FormControl>
                </Box>
              </Box>
            </Paper>

            {/* Stats Bar */}
            <Paper 
              elevation={0}
              sx={{ 
                p: 2,
                bgcolor: alpha(theme.palette.success.main, 0.1),
                border: `1px solid ${alpha(theme.palette.success.main, 0.2)}`,
                borderRadius: 3,
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: 2
              }}
            >

              <Typography variant="body2" sx={{ fontWeight: 600, color: 'text.primary' }}>
                Showing {filteredMints.length} of {nfts.length} NFTs
                
              </Typography>
              <Box sx={{ display: 'flex', gap: 2 }}>
                <Chip
                  label={`${nfts.filter(nft => nft.status === "Minted").length} Minted`}
                  color="success"
                  variant="outlined"
                  size="small"
                  sx={{ fontWeight: 600 }}
                />
                <Chip
                  label={`${nfts.filter(nft => nft.status === "Not minted").length} Pending`}
                  color="warning"
                  variant="outlined"
                  size="small"
                  sx={{ fontWeight: 600 }}
                />
              </Box>
            </Paper>

            {/* View Tabs */}
            <Box sx={{ width: "100%" }}>
              <Paper 
                elevation={0}
                sx={{ 
                  bgcolor: alpha(theme.palette.background.paper, 0.5),
                  border: `1px solid ${alpha(theme.palette.divider, 0.1)}`,
                  borderRadius: 3,
                  p: 1,
                  mb: 3
                }}
              >
                <Tabs 
                  value={tabValue} 
                  onChange={handleTabChange} 
                  sx={{ 
                    '& .MuiTab-root': {
                      borderRadius: 2,
                      textTransform: 'none',
                      fontWeight: 600,
                      minHeight: 48,
                    },
                    '& .Mui-selected': {
                      bgcolor: alpha(theme.palette.primary.main, 0.1),
                      color: 'primary.main',
                    }
                  }}
                >
                  <Tab 
                    label="Grid View" 
                    value="grid" 
                    icon={<GridViewIcon />} 
                    iconPosition="start"
                  />
                  <Tab 
                    label="List View" 
                    value="list" 
                    icon={<ListViewIcon />} 
                    iconPosition="start"
                  />
                </Tabs>
              </Paper>

              {/* Loading State */}
              {loading ? (
                <Paper 
                  elevation={0}
                  sx={{ 
                    display: "flex", 
                    flexDirection: "column",
                    justifyContent: "center", 
                    alignItems: "center", 
                    minHeight: 400,
                    bgcolor: alpha(theme.palette.background.paper, 0.5),
                    border: `1px solid ${alpha(theme.palette.divider, 0.1)}`,
                    borderRadius: 4
                  }}
                >
                  <CircularProgress sx={{ mb: 2, color: 'primary.main' }} />
                  <Typography variant="h6" color="text.secondary" sx={{ fontWeight: 500 }}>
                    Loading NFTs...
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                    Please wait while we fetch your collection
                  </Typography>
                </Paper>
              ) : (
                <>
                  {/* Grid View */}
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
                            <LazyImage
                              src={mint.image}
                              alt={mint.name}
                              sx={{
                                width: "100%",
                                height: 220,
                                aspectRatio: "1/1",
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

                  {/* List View */}
                  {tabValue === "list" && (
                    <Paper 
                      variant="outlined" 
                      sx={{ 
                        borderRadius: 4,
                        overflow: 'hidden',
                        border: `1px solid ${alpha(theme.palette.divider, 0.1)}`,
                      }}
                    >
                      <Box
                        sx={{
                          display: "grid",
                          gridTemplateColumns: {
                            xs: "1fr 2fr 2fr 1fr",
                            md: "80px 2fr 2fr 2fr 1fr",
                          },
                          gap: 2,
                          p: 3,
                          fontWeight: "bold",
                          borderBottom: `2px solid ${alpha(theme.palette.divider, 0.1)}`,
                          bgcolor: alpha(theme.palette.primary.main, 0.05),
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
                      
                      {filteredMints.map((mint, index) => (
                        <Box
                          key={mint.id}
                          sx={{
                            display: "grid",
                            gridTemplateColumns: {
                              xs: "1fr 2fr 2fr 1fr",
                              md: "80px 2fr 2fr 2fr 1fr",
                            },
                            gap: 2,
                            p: 3,
                            alignItems: "center",
                            borderBottom: index === filteredMints.length - 1 ? 'none' : `1px solid ${alpha(theme.palette.divider, 0.1)}`,
                            transition: 'background-color 0.2s ease',
                            "&:hover": { 
                              bgcolor: alpha(theme.palette.action.hover, 0.5),
                            },
                          }}
                        >
                          <Box sx={{ display: { xs: "none", md: "block" } }}>
                            <LazyImage
                              src={mint.image}
                              alt={mint.name}
                              sx={{ 
                                width: 56, 
                                height: 56,
                                borderRadius: 2,
                                border: `2px solid ${alpha(theme.palette.divider, 0.1)}`
                              }}
                            />
                          </Box>
                          
                          <Typography sx={{ fontWeight: "bold", fontSize: '1rem' }}>
                            {mint.name}
                          </Typography>
                          
                          <Box>
                            <Tooltip title={mint.txHash}>
                              <Typography 
                                variant="body2" 
                                noWrap 
                                sx={{ 
                                  fontFamily: 'monospace',
                                  bgcolor: alpha(theme.palette.background.default, 0.5),
                                  px: 1,
                                  py: 0.5,
                                  borderRadius: 1,
                                  display: 'inline-block',
                                  fontSize: '0.85rem'
                                }}
                              >
                                {truncate(mint.txHash, 16)}
                              </Typography>
                            </Tooltip>
                            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.5 }}>
                              {formatDate(mint.timestamp)}
                            </Typography>
                          </Box>
                          
                          <Box sx={{ display: { xs: "none", md: "block" } }}>
                            <Tooltip title={mint.recipient}>
                              <Typography
                                variant="body2"
                                noWrap
                                sx={{ 
                                  fontFamily: 'monospace',
                                  bgcolor: alpha(theme.palette.background.default, 0.5),
                                  px: 1,
                                  py: 0.5,
                                  borderRadius: 1,
                                  fontSize: '0.85rem'
                                }}
                              >
                                {truncate(mint.recipient, 20)}
                              </Typography>
                            </Tooltip>
                          </Box>
                          
                          <Box>
                            <Chip
                              label={mint.status}
                              color={mint.status === "Minted" ? "success" : "warning"}
                              size="small"
                              sx={{ fontWeight: 600 }}
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
      </Card>
    </Box>
  );
};

export default MintingDashboard;