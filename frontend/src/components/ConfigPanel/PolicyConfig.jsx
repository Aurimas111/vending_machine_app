import React from "react";
import {
  Typography,
  Box,
  FormControl,
  FormLabel,
  FormHelperText,
  Button,
  TextField,
  Paper,
  alpha,
} from "@mui/material";
import { Delete, Create, Policy } from "@mui/icons-material";
import { Controller } from "react-hook-form";
import { useTheme } from "@mui/material/styles";

const PolicyConfig = ({
  policyId,
  collectionNameValue,
  nftCount,
  control,
  policyError,
  policyLockEpochValue,
  handleDeletePolicy,
  handleCreatePolicy,
  setSaveSuccess,
  setPolicyError,
}) => {
  const theme = useTheme();

  return (
    <>
      <Box sx={{ display: "flex", alignItems: "center", gap: 2, mb: 3 }}>
        <Box sx={{ 
          width: 40, 
          height: 40, 
          borderRadius: 2, 
          bgcolor: alpha(theme.palette.primary.main, 0.1),
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center'
        }}>
          <Policy sx={{ fontSize: 20, color: 'primary.main' }} />
        </Box>
        <Typography variant="h6" sx={{ fontWeight: 600 }}>
          Policy Configuration
        </Typography>
      </Box>

      {policyId ? (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 3 }}>
          <Paper
            elevation={0}
            sx={{
              p: 3,
              borderRadius: 2,
              bgcolor: alpha(theme.palette.success.main, 0.05),
              border: `1px solid ${alpha(theme.palette.success.main, 0.2)}`,
            }}
          >
            <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
              <Typography variant="body1" sx={{ 
                wordBreak: "break-word",
                fontSize: '0.95rem',
                lineHeight: 1.6
              }}>
                <Typography component="span" sx={{ fontWeight: 600, color: 'text.primary' }}>
                  Collection name:
                </Typography>{" "}
                <Typography component="span" sx={{ color: 'text.secondary' }}>
                  {collectionNameValue}
                </Typography>
              </Typography>
              
              <Typography variant="body1" sx={{ fontSize: '0.95rem', lineHeight: 1.6 }}>
                <Typography component="span" sx={{ fontWeight: 600, color: 'text.primary' }}>
                  Policy ID:
                </Typography>{" "}
                <Typography 
                  component="span" 
                  sx={{ 
                    color: 'text.secondary',
                    fontFamily: 'monospace',
                    fontSize: '0.9rem',
                    wordBreak: 'break-all'
                  }}
                >
                  {policyId}
                </Typography>
              </Typography>
              
              <Typography variant="body1" sx={{ fontSize: '0.95rem', lineHeight: 1.6 }}>
                <Typography component="span" sx={{ fontWeight: 600, color: 'text.primary' }}>
                  NFTs associated with policy:
                </Typography>{" "}
                <Typography component="span" sx={{ color: 'text.secondary' }}>
                  {nftCount}
                </Typography>
              </Typography>
            </Box>
          </Paper>

          <Button
            variant="outlined"
            color="error"
            size="large"
            onClick={handleDeletePolicy}
            startIcon={<Delete />}
            sx={{ 
              borderRadius: 2,
              px: 3,
              py: 1.5,
              fontWeight: 600,
              textTransform: 'none',
              alignSelf: 'flex-start'
            }}
          >
            Delete Policy
          </Button>
        </Box>
      ) : (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 3 }}>
          <Paper
            elevation={0}
            sx={{
              p: 3,
              borderRadius: 2,
              bgcolor: alpha(theme.palette.warning.main, 0.05),
              border: `1px solid ${alpha(theme.palette.warning.main, 0.2)}`,
            }}
          >
            <Typography variant="body1" sx={{ 
              fontWeight: 600,
              color: 'warning.main',
              textAlign: 'center',
              py: 1
            }}>
              No policy is currently set.
            </Typography>
          </Paper>

          <Box sx={{ display: "flex", flexDirection: "column", gap: 3 }}>
            <Controller
              control={control}
              name="collectionName"
              render={({ field }) => (
                <FormControl fullWidth>
                  <FormLabel sx={{ mb: 1, fontWeight: 600 }}>
                    Collection Name
                  </FormLabel>
                  <TextField
                    {...field}
                    placeholder="My Awesome NFT Collection"
                    InputProps={{ 
                      readOnly: false,
                      sx: {
                        borderRadius: 2,
                        '& .MuiOutlinedInput-notchedOutline': {
                          borderColor: alpha(theme.palette.divider, 0.3),
                        },
                        '&:hover .MuiOutlinedInput-notchedOutline': {
                          borderColor: alpha(theme.palette.primary.main, 0.5),
                        }
                      }
                    }}
                    sx={{ 
                      '& .MuiInputBase-root': {
                        bgcolor: alpha(theme.palette.background.paper, 0.8)
                      }
                    }}
                  />
                  <FormHelperText sx={{ mt: 1, fontSize: '0.8rem' }}>
                    The name of your NFT collection.
                  </FormHelperText>
                </FormControl>
              )}
            />

            <FormControl fullWidth>
              <FormLabel sx={{ mb: 1, fontWeight: 600 }}>
                Policy locking period (epochs)
              </FormLabel>
              <Controller
                control={control}
                name="policyLockEpoch"
                render={({ field }) => (
                  <TextField
                    {...field}
                    type="number"
                    placeholder="Enter epoch number"
                    InputProps={{ 
                      readOnly: false, 
                      inputProps: { min: 1 },
                      sx: {
                        borderRadius: 2,
                        '& .MuiOutlinedInput-notchedOutline': {
                          borderColor: alpha(theme.palette.divider, 0.3),
                        },
                        '&:hover .MuiOutlinedInput-notchedOutline': {
                          borderColor: alpha(theme.palette.primary.main, 0.5),
                        }
                      }
                    }}
                    sx={{ 
                      '& .MuiInputBase-root': {
                        bgcolor: alpha(theme.palette.background.paper, 0.8)
                      }
                    }}
                  />
                )}
              />
              <FormHelperText sx={{ mt: 1, fontSize: '0.8rem' }}>
                This defines the epoch after which the policy will be locked.
              </FormHelperText>
              {policyError && (
                <FormHelperText error sx={{ mt: 1, fontSize: '0.8rem' }}>
                  {policyError}
                </FormHelperText>
              )}
            </FormControl>

            <Button
              variant="contained"
              color="primary"
              size="large"
              onClick={() => {
                // manual validation for collectionName and policyLockEpoch
                let valid = true;
                let errorMsg = "";

                if (!collectionNameValue || collectionNameValue.trim().length < 3) {
                  errorMsg = "Collection name is required and must be at least 3 characters.";
                  valid = false;
                } else if (
                  !policyLockEpochValue ||
                  isNaN(Number(policyLockEpochValue)) ||
                  Number(policyLockEpochValue) < 1
                ) {
                  errorMsg = "Policy locking period must be a positive number.";
                  valid = false;
                }

                if (!valid) {
                  setSaveSuccess(false);
                  setPolicyError(errorMsg);
                  return;
                }

                handleCreatePolicy();
              }}
              startIcon={<Create />}
              sx={{ 
                borderRadius: 2,
                px: 3,
                py: 1.5,
                fontWeight: 600,
                textTransform: 'none',
                alignSelf: 'flex-start',
                boxShadow: `0 4px 14px 0 ${alpha(theme.palette.primary.main, 0.3)}`,
                '&:hover': {
                  boxShadow: `0 6px 20px 0 ${alpha(theme.palette.primary.main, 0.4)}`
                }
              }}
              disabled={!collectionNameValue || !policyLockEpochValue}
            >
              Create Policy
            </Button>
          </Box>
        </Box>
      )}
    </>
  );
};

export default PolicyConfig;