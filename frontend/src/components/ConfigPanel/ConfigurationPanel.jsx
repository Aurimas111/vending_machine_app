import React, { use, useState, useEffect, useRef } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm, Controller, set } from "react-hook-form";
import * as z from "zod";
import {
  Card,
  CardContent,
  CardHeader,
  Typography,
  Button,
  FormControl,
  FormHelperText,
  FormLabel,
  Box,
  Paper,
  alpha,
} from "@mui/material";
import { Save, Delete, Create, Settings, DataObject } from "@mui/icons-material";
import CodeEditor from "@uiw/react-textarea-code-editor";
import { useTheme } from "@mui/material/styles";
import configService from "../../services/configService";
import PolicyConfig from "./PolicyConfig";
import MintingParameters from "./MintingParameters";

const DEFAULT_FORM_VALUES = {
  collectionName: "",
  nftPrice: "10000000", // 10 ADA in lovelace
  collectionSize: "0",
  nftsReservedPerTx: "5",
  nftsToMintPerTx: "5",
  nftsToNotMint: "0",
  refundsPerTxLimit: "3",
  policyLockEpoch: "50",
  nftMetadata: JSON.stringify(
    [
      {
        name: "",
        image: "",
        attributes: [{"trait_type": "Example Trait", "value": "Example Value"}],
      },
    ],
    null,
    2,
  ),
};

const formSchema = z.object({
  nftsReservedPerTx: z
    .string()
    .refine((val) => !isNaN(Number(val)) && Number(val) > 0, {
      message: "NFTs to mint per transaction must be a positive number",
    }),
  nftsToMintPerTx: z
    .string()
    .refine((val) => !isNaN(Number(val)) && Number(val) > 0, {
      message: "NFTs to mint per transaction must be a positive number",
    }),
  nftsToNotMint: z
    .string()
    .refine((val) => !isNaN(Number(val)) && Number(val) >= 0, {
      message: "NFTs to not mint must be a non-negative number",
    }),
  refundsPerTxLimit: z
    .string()
    .refine((val) => !isNaN(Number(val)) && Number(val) >= 0, {
      message: "Refunds per transaction limit must be a positive number",
    }),
});

const ConfigurationPanel = ({
  isVendingMachineActive = false,
  walletAddress,
}) => {
  
  const [saveSuccess, setSaveSuccess] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [policyId, setPolicyId] = useState("");
  const [nftCount, setNftCount] = useState(0);
  const [isEditingParams, setIsEditingParams] = useState(false);
  const [metadataExists, setMetadataExists] = useState(false);
  const [policyError, setPolicyError] = useState("");
  const [metadataError, setMetadataError] = useState("");

  const {
    control,
    handleSubmit,
    reset,
    watch,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(formSchema),
    defaultValues: DEFAULT_FORM_VALUES,
  });

  const nftMetadataValue = watch("nftMetadata");
  const collectionNameValue = watch("collectionName");
  const policyLockEpochValue = watch("policyLockEpoch");
  const nftPrice = watch("nftPrice");
  const nftsReservedPerTx = watch("nftsReservedPerTx");
  const nftsToMintPerTx = watch("nftsToMintPerTx");
  const nftsToNotMint = watch("nftsToNotMint");
  const refundsPerTxLimit = watch("refundsPerTxLimit");

  const theme = useTheme();

  const handleDeletePolicy = async () => {
    try {
      await configService.deletePolicy();
      reset({
      ...DEFAULT_FORM_VALUES,
      // preserve current parameter values
      nftPrice,
      nftsReservedPerTx,
      nftsToMintPerTx,
      nftsToNotMint,
      refundsPerTxLimit,
    });
      setMetadataExists(false);
      setPolicyId("");
      setNftCount(0);
      setSaveSuccess(null);
    } catch (error) {
      console.error("Error deleting policy:", error);
      setSaveSuccess(false);
    }
  };

  useEffect(() => {

  if (!walletAddress) return;

  setIsLoading(true);
  configService.getConfig()
    .then(config => {
      if (config) {
          const metadataList = (config.data.metadataList || []).map(item => {
            const { dynamicAttributes, ...rest } = item;
            return {
              ...rest,
              attributes: dynamicAttributes
                ? Object.entries(dynamicAttributes).map(([trait_type, value]) => ({
                    trait_type,
                    value,
                  }))
                : [],
            };
          });

         const prettyMetadata =
          metadataList.length > 0
            ? JSON.stringify(metadataList, null, 2)
            : JSON.stringify([
                {
                  name: "",
                  description: "",
                  file_url: "",
                  image: "",
                  attributes: [
                    { trait_type: "Example Trait", value: "Example Value" }
                  ]
                }
              ], null, 2);

        reset({
          ...DEFAULT_FORM_VALUES,
          ...config.data,
          nftMetadata: prettyMetadata,
          collectionName: config.data.collectionName ?? "",
          nftPrice: config.data.nftPrice?.toString() ?? "10000000",
          collectionSize: config.data.collectionSize?.toString() ?? "0",
          nftsReservedPerTx: config.data.nftsReservedPerTx?.toString() ?? "5",
          nftsToMintPerTx: config.data.nftsToMintPerTx?.toString() ?? "5",
          nftsToNotMint: config.data.nftsToNotMint?.toString() ?? "0",
          refundsPerTxLimit: config.data.refundsPerTxLimit?.toString() ?? "3",
          policyLockEpoch: config.data.policy?.policyLockEpoch?.toString() ?? "50",
        });

        setPolicyId(config.data.policy?.policyId || "");
        setNftCount(config.data.metadataList?.length || 0);

        const value = config.data.metadataList && config.data.metadataList.length > 0
          ? JSON.stringify(config.data.metadataList)
          : "";
        try {
          const parsed = JSON.parse(value);
          setMetadataExists(
            Array.isArray(parsed) &&
            parsed.length > 0 &&
            parsed.some(m => m.name || m.image || (m.attributes && m.attributes.length > 0))
          );
        } catch {
          setMetadataExists(false);
        }
      }
    })
    .catch(error => {
      console.error("Error fetching configuration:", error);
    })
    .finally(() => {
      setIsLoading(false);
    });
}, [walletAddress, reset]);


  const handleDeleteMetadata = async () => {
    try {
      await configService.deleteMetadata();
          reset({
      ...DEFAULT_FORM_VALUES,
      collectionName: collectionNameValue,
      policyLockEpoch: policyLockEpochValue,
      nftPrice,
      nftsReservedPerTx,
      nftsToMintPerTx,
      nftsToNotMint,
      refundsPerTxLimit,
    });
      setNftCount(0);
      setSaveSuccess(null);
      setMetadataExists(false);
    } catch (error) {
      console.error("Error deleting metadata:", error);
      setSaveSuccess(false);
    }
  };

  const handleCreatePolicy = async () => {
    try {
      const response = await configService.createPolicy({ 
        address: walletAddress,
        collectionName: collectionNameValue,
        policyLockEpoch: policyLockEpochValue,
        nftPrice: nftPrice,
        nftsReservedPerTx: nftsReservedPerTx,
        nftsToMintPerTx: nftsToMintPerTx,
        nftsToNotMint: nftsToNotMint,
        refundsPerTxLimit: refundsPerTxLimit,
      });
      setPolicyId(response.data || "");
      setNftCount(0); // Reset NFT count
      setSaveSuccess(null);
    } catch (error) {
      console.error("Error creating policy:", error);
      setSaveSuccess(false);
    }
  }

  const handleCreateMetadata = async () => {
    try {
      const metadata = JSON.parse(nftMetadataValue);
      const response = await configService.createMetadata({
        address: walletAddress,
        metadata: metadata,
      });
      setMetadataExists(true);
      setNftCount(metadata.length || 0);
      setSaveSuccess(null);
    } catch (error) {
      console.error("Error creating metadata:", error);
      setSaveSuccess(false);
    }
  };

  const saveParams = async () => {
    try {
      const enrichedData = {
        nftPrice,
        nftsReservedPerTx,
        nftsToMintPerTx,
        nftsToNotMint,
        refundsPerTxLimit,
        walletAddress,
      };
      await configService.setParameters(enrichedData);
      setSaveSuccess(true);
      setIsEditingParams(false);
    } catch (error) {
      console.error("Error saving parameters:", error);
      setSaveSuccess(false);
      setIsEditingParams(false);
    }
  };

  return (
    <Card 
      sx={{ 
        width: "100%", 
        bgcolor: "background.paper",
        borderRadius: 4,
        boxShadow: theme.palette.mode === 'dark' 
          ? '0 8px 32px rgba(0, 0, 0, 0.3)' 
          : '0 8px 32px rgba(0, 0, 0, 0.08)',
        border: `1px solid ${alpha(theme.palette.divider, 0.1)}`,
        overflow: 'hidden'
      }}
    >
      <CardHeader
        sx={{
          background: `linear-gradient(135deg, ${alpha(theme.palette.primary.main, 0.05)}, ${alpha(theme.palette.secondary.main, 0.05)})`,
          borderBottom: `1px solid ${alpha(theme.palette.divider, 0.1)}`,
          pb: 3
        }}
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
              <Settings sx={{ fontSize: 24, color: 'primary.main' }} />
            </Box>
            <Box>
              <Typography variant="h5" sx={{ fontWeight: 700, mb: 0.5 }}>
                Minting Configuration
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ fontSize: '0.9rem' }}>
                Configure your NFT vending machine settings and collection parameters
              </Typography>
            </Box>
          </Box>
        }
      />
      
      <CardContent sx={{ p: 4, '& > *:not(:last-child)': { mb: 4 } }}>
        {/* Policy Configuration Section */}
        <Paper 
          elevation={0}
          sx={{ 
            p: 3, 
            borderRadius: 3,
            bgcolor: alpha(theme.palette.background.default, 0.5),
            border: `1px solid ${alpha(theme.palette.divider, 0.1)}`
          }}
        >
          <PolicyConfig
            policyId={policyId}
            collectionNameValue={collectionNameValue}
            nftCount={nftCount}
            control={control}
            policyError={policyError}
            policyLockEpochValue={policyLockEpochValue}
            handleDeletePolicy={handleDeletePolicy}
            handleCreatePolicy={handleCreatePolicy}
            setSaveSuccess={setSaveSuccess}
            setPolicyError={setPolicyError}
          />
        </Paper>

        {/* NFT Metadata Section */}
        {policyId && (
          <Paper 
            elevation={0}
            sx={{ 
              p: 3, 
              borderRadius: 3,
              bgcolor: alpha(theme.palette.background.default, 0.5),
              border: `1px solid ${alpha(theme.palette.divider, 0.1)}`
            }}
          >
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
                <DataObject sx={{ fontSize: 20, color: 'secondary.main' }} />
              </Box>
              <Typography variant="h6" sx={{ fontWeight: 600 }}>
                NFT Metadata
              </Typography>
            </Box>
            
            <Controller
              control={control}
              name="nftMetadata"
              render={({ field }) => (
                <FormControl fullWidth error={!!errors.nftMetadata}>
                  <FormLabel sx={{ mb: 1, fontWeight: 600 }}>
                    Collection Metadata (JSON)
                  </FormLabel>
                  <Paper
                    elevation={0}
                    sx={{
                      borderRadius: 2,
                      border: `1px solid ${alpha(theme.palette.divider, 0.2)}`,
                      overflow: 'hidden',
                      bgcolor: theme.palette.background.paper
                    }}
                  >
                    <Box
                      sx={{
                        fontSize: 14,
                        whiteSpace: "pre-wrap",
                        fontFamily: "monospace",
                        minHeight: 200,
                        maxHeight: 300,
                        overflow: "auto",
                        p: 2
                      }}
                      data-color-mode="light"
                    >
                      {(metadataExists) ? (
                        <pre style={{ margin: 0, color: theme.palette.text.primary }}>{field.value}</pre>
                      ) : (
                        <CodeEditor
                          value={field.value}
                          onChange={(e) => field.onChange(e.target.value)}
                          highlight={(code) => highlight(code, languages.json, "json")}
                          padding={0}
                          style={{
                            fontSize: 14,
                            fontFamily: "monospace",
                            backgroundColor: 'transparent',
                            minHeight: 200,
                            maxHeight: 300,
                            width: "100%",
                            border: 'none',
                            outline: "none",
                            whiteSpace: "pre-wrap",
                            overflow: "auto",
                            color: theme.palette.text.primary
                          }}
                        />
                      )}
                    </Box>
                  </Paper>
                  <FormHelperText sx={{ mt: 1, fontSize: '0.8rem' }}>
                    JSON format metadata for your NFT collection. Once metadata is uploaded it's not changeable.
                    If you want to change it, you need to delete it and upload the new one. If metadata is deleted and reuploaded, the mint will start from 0.
                  </FormHelperText>
                  {metadataError && (
                    <FormHelperText error sx={{ mt: 1 }}>{metadataError}</FormHelperText>
                  )}
                </FormControl>
              )}
            />

            <Box sx={{ mt: 3 }}>
              {metadataExists ? (
                <Button
                  variant="outlined"
                  color="warning"
                  size="large"
                  onClick={handleDeleteMetadata}
                  startIcon={<Delete />}
                  sx={{ 
                    borderRadius: 2,
                    px: 3,
                    py: 1.5,
                    fontWeight: 600,
                    textTransform: 'none'
                  }}
                >
                  Delete Metadata
                </Button>
              ) : (
                <Button
                  variant="contained"
                  color="primary"
                  size="large"
                  onClick={async () => {
                      // Manual validation for metadata
                      let errorMsg= "";
                      let valid = true;
                      if (!control._formValues.nftMetadata) {
                        errorMsg = "Metadata field cannot be empty."
                        valid = false;
                      }
                      let parsed;
                      try {
                        parsed = JSON.parse(control._formValues.nftMetadata);
                      } catch {
                        errorMsg = "Invalid JSON format in metadata.";
                        valid = false;
                      }

                      if (!valid) {
                        setSaveSuccess(false);
                        setMetadataError(errorMsg);
                        return;
                      }
                      // If valid, proceed
                      handleCreateMetadata();
                    }}
                  startIcon={<Create />}
                  sx={{ 
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
                  disabled={
                    !control._formValues.nftMetadata
                  }
                >
                  Upload Metadata
                </Button>
              )}
            </Box>
          </Paper>
        )}

        {/* Minting Parameters Section */}
        <Paper 
          elevation={0}
          sx={{ 
            p: 3, 
            borderRadius: 3,
            bgcolor: alpha(theme.palette.background.default, 0.5),
            border: `1px solid ${alpha(theme.palette.divider, 0.1)}`
          }}
        >
          <MintingParameters
            isEditingParams={isEditingParams}
            control={control}
            errors={errors}
            nftPrice={nftPrice}
            nftCount={nftCount}
            nftsReservedPerTx={nftsReservedPerTx}
            nftsToMintPerTx={nftsToMintPerTx}
            nftsToNotMint={nftsToNotMint}
            refundsPerTxLimit={refundsPerTxLimit}
            saveSuccess={saveSuccess}
            handleSubmit={handleSubmit}
            saveParams={saveParams}
            setIsEditingParams={setIsEditingParams}
          />
        </Paper>
      </CardContent>
    </Card>
  );
};

export default ConfigurationPanel;