import React from "react";
import {
  Typography,
  TextField,
  Button,
  Alert,
  FormControl,
  FormHelperText,
  FormLabel,
  Box,
  CardActions,
  Paper,
  alpha,
} from "@mui/material";
import { Save, Create, Settings } from "@mui/icons-material";
import { Controller } from "react-hook-form";
import { useTheme } from "@mui/material/styles";

const MintingParameters = ({
  isEditingParams,
  control,
  errors,
  nftPrice,
  nftCount,
  nftsReservedPerTx,
  nftsToMintPerTx,
  nftsToNotMint,
  refundsPerTxLimit,
  saveSuccess,
  handleSubmit,
  saveParams,
  setIsEditingParams,
}) => {
  const theme = useTheme();

  const renderFormField = (name, label, helperText, minValue = 0, fieldProps = {}) => (
    <Controller
      control={control}
      name={name}
      render={({ field }) => (
        <FormControl fullWidth error={!!errors[name]}>
          <FormLabel sx={{ mb: 1, fontWeight: 600 }}>
            {label}
          </FormLabel>
          <TextField
            {...field}
            type="number"
            error={!!errors[name]}
            InputProps={{ 
              readOnly: false, 
              inputProps: { min: minValue },
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
            {...fieldProps}
          />
          <FormHelperText sx={{ mt: 1, fontSize: '0.8rem' }}>
            {helperText}
          </FormHelperText>
          {errors[name] && (
            <FormHelperText error sx={{ mt: 0.5, fontSize: '0.8rem' }}>
              {errors[name].message}
            </FormHelperText>
          )}
        </FormControl>
      )}
    />
  );

  const renderReadOnlyField = (value, label, helperText, disabled = false) => (
    <FormControl fullWidth>
      <FormLabel sx={{ mb: 1, fontWeight: 600 }}>
        {label}
      </FormLabel>
      <TextField
        value={value}
        type="number"
        InputProps={{ 
          readOnly: true,
          sx: {
            borderRadius: 2,
            bgcolor: alpha(theme.palette.action.hover, 0.3),
            '& .MuiOutlinedInput-notchedOutline': {
              borderColor: alpha(theme.palette.divider, 0.2),
            }
          }
        }}
        disabled={disabled}
      />
      <FormHelperText sx={{ mt: 1, fontSize: '0.8rem' }}>
        {helperText}
      </FormHelperText>
    </FormControl>
  );

  return (
    <>
      <Box sx={{ display: "flex", alignItems: "center", gap: 2, mb: 3 }}>
        <Box sx={{ 
          width: 40, 
          height: 40, 
          borderRadius: 2, 
          bgcolor: alpha(theme.palette.secondary.main, 0.1),
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center'
        }}>
          <Settings sx={{ fontSize: 20, color: 'secondary.main' }} />
        </Box>
        <Typography variant="h6" sx={{ fontWeight: 600 }}>
          Minting Parameters
        </Typography>
      </Box>
      
      {isEditingParams ? (
        <form
          style={{ display: "flex", flexDirection: "column", gap: "24px" }}
          onSubmit={handleSubmit(saveParams)}
        >
          <Box sx={{ display: "flex", flexDirection: "column", gap: 3 }}>
            <Box
              sx={{
                display: "grid",
                gridTemplateColumns: { xs: "1fr", md: "1fr 1fr" },
                gap: 3,
              }}
            >
              {renderFormField(
                "nftPrice",
                "NFT Price (Lovelace)",
                "The price in Lovelace required to mint one NFT (1 ADA = 1,000,000 Lovelace).",
                1000000
              )}

              <FormControl fullWidth>
                <FormLabel sx={{ mb: 1, fontWeight: 600 }}>
                  Collection Size
                </FormLabel>
                <TextField
                  value={nftCount}
                  type="number"
                  InputProps={{
                    readOnly: true,
                    sx: {
                      borderRadius: 2,
                      bgcolor: alpha(theme.palette.action.hover, 0.3),
                      '& .MuiOutlinedInput-notchedOutline': {
                        borderColor: alpha(theme.palette.divider, 0.2),
                      }
                    }
                  }}
                  disabled
                />
                <FormHelperText sx={{ mt: 1, fontSize: '0.8rem' }}>
                  The total number of NFTs in the collection. Collection size is the amount of NFT metadata entries that have been uploaded.
                </FormHelperText>
              </FormControl>
            </Box>

            <Box
              sx={{
                display: "grid",
                gridTemplateColumns: { xs: "1fr", md: "1fr 1fr" },
                gap: 3,
              }}
            >
              {renderFormField(
                "nftsReservedPerTx",
                "NFTs Reserved Per Transaction",
                "Maximum number of NFTs that can be reserved in a single transaction.",
                1
              )}

              {renderFormField(
                "nftsToMintPerTx",
                "NFTs to Mint Per Transaction",
                "Maximum number of NFTs that can be minted in a single transaction.",
                1
              )}
            </Box>

            <Box
              sx={{
                display: "grid",
                gridTemplateColumns: { xs: "1fr", md: "1fr 1fr" },
                gap: 3,
              }}
            >
              {renderFormField(
                "nftsToNotMint",
                "NFTs to Not Mint",
                "Number of NFTs in the collection that should not be minted.",
                0
              )}

              {renderFormField(
                "refundsPerTxLimit",
                "Refunds Per Transaction Limit",
                "Maximum number of refunds allowed per transaction.",
                1
              )}
            </Box>
          </Box>

          {saveSuccess === false && (
            <Alert 
              severity="error" 
              sx={{ 
                mt: 2,
                borderRadius: 2,
                border: `1px solid ${alpha(theme.palette.error.main, 0.2)}`,
                bgcolor: alpha(theme.palette.error.main, 0.05)
              }}
            >
              Failed to save parameters. Please try again.
            </Alert>
          )}

          {saveSuccess === true && (
            <Alert 
              severity="success" 
              sx={{ 
                mt: 2,
                borderRadius: 2,
                border: `1px solid ${alpha(theme.palette.success.main, 0.2)}`,
                bgcolor: alpha(theme.palette.success.main, 0.05)
              }}
            >
              Parameters saved successfully!
            </Alert>
          )}

          <CardActions
            sx={{
              display: "flex",
              justifyContent: "flex-end",
              px: 0,
              mt: 2,
            }}
          >
            <Button
              type="submit"
              variant="contained"
              startIcon={<Save />}
              color="primary"
              sx={{ 
                width: "100%",
                borderRadius: 2,
                px: 3,
                py: 1.5,
                fontWeight: 600,
                textTransform: 'none',
                background: `linear-gradient(45deg, ${theme.palette.primary.main}, ${theme.palette.primary.dark})`,
                boxShadow: `0 4px 14px 0 ${alpha(theme.palette.primary.main, 0.3)}`,
                '&:hover': {
                  boxShadow: `0 6px 20px 0 ${alpha(theme.palette.primary.main, 0.4)}`
                }
              }}
              size="large"
            >
              Save Parameters
            </Button>
          </CardActions>
        </form>
      ) : (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 3 }}>
          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: { xs: "1fr", md: "1fr 1fr" },
              gap: 3,
            }}
          >
            {renderReadOnlyField(
              nftPrice,
              "NFT Price (Lovelace)",
              "The price in Lovelace required to mint one NFT (1 ADA = 1,000,000 Lovelace)."
            )}
            
            {renderReadOnlyField(
              nftCount,
              "Collection Size",
              "The total number of NFTs in the collection. Collection size is the amount of NFT metadata entries that have been uploaded.",
              true
            )}
          </Box>
          
          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: { xs: "1fr", md: "1fr 1fr" },
              gap: 3,
            }}
          >
            {renderReadOnlyField(
              nftsReservedPerTx,
              "NFTs Reserved Per Transaction",
              "Maximum number of NFTs that can be reserved in a single transaction."
            )}
            
            {renderReadOnlyField(
              nftsToMintPerTx,
              "NFTs to Mint Per Transaction",
              "Maximum number of NFTs that can be minted in a single transaction."
            )}
          </Box>
          
          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: { xs: "1fr", md: "1fr 1fr" },
              gap: 3,
            }}
          >
            {renderReadOnlyField(
              nftsToNotMint,
              "NFTs to Not Mint",
              "Number of NFTs in the collection that should not be minted."
            )}
            
            {renderReadOnlyField(
              refundsPerTxLimit,
              "Refunds Per Transaction Limit",
              "Maximum number of refunds allowed per transaction."
            )}
          </Box>
          
          <CardActions
            sx={{
              display: "flex",
              justifyContent: "flex-end",
              px: 0,
              mt: 2,
            }}
          >
            <Button
              type="button"
              onClick={() => setIsEditingParams(true)}
              variant="outlined"
              startIcon={<Create />}
              color="primary"
              sx={{ 
                width: "100%",
                borderRadius: 2,
                px: 3,
                py: 1.5,
                fontWeight: 600,
                textTransform: 'none'
              }}
              size="large"
            >
              Edit Parameters
            </Button>
          </CardActions>
        </Box>
      )}
    </>
  );
};

export default MintingParameters;