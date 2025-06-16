import React, { use, useState, useEffect, useRef } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm, Controller, set } from "react-hook-form";
import * as z from "zod";
import {
  Card,
  CardContent,
  CardHeader,
  CardActions,
  Typography,
  TextField,
  Button,
  Divider,
  Alert,
  FormControl,
  FormHelperText,
  FormLabel,
  Box,
  Badge,
} from "@mui/material";
import { Save, Delete, Create } from "@mui/icons-material";
import CodeEditor from "@uiw/react-textarea-code-editor";
import { useTheme } from "@mui/material/styles";
import configService from "../services/configService";

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
    .refine((val) => !isNaN(Number(val)) && Number(val) >= 0, {
      message: "NFTs reserved per transaction must be a non-negative number",
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
      message: "Refunds per transaction limit must be a non-negative number",
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

  const handleDeletePolicy = async () => {
    try {
      await configService.deletePolicy({ address: walletAddress });
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

  const theme = useTheme();

  useEffect(() => {

  if (!walletAddress) return;
  
  setIsLoading(true);
  configService.getConfig({ address: walletAddress })
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
      await configService.deleteMetadata({ address: walletAddress });
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
    <Card sx={{ width: "100%", bgcolor: "background.paper" }}>
      <CardHeader
        title="NFT Vending Machine Configuration"
        subheader="Configure your NFT vending machine settings. These settings determine how your NFTs will be minted when transactions are received."
        action={
          <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
            <Badge
              color={isVendingMachineActive ? "success" : "default"}
              variant="dot"
              anchorOrigin={{ vertical: "top", horizontal: "left" }}
              sx={{ mr: 1 }}
            >
            </Badge>
          </Box>
        }
      />
      <CardContent>
        <Typography variant="h6" sx={{pb: 2}}>Policy configuration</Typography>

        {policyId ? (
          <Box sx={{ display: "flex", flexDirection: "column", gap: "16px", pb: 2 }}>
            <Typography variant="body1" sx={{ wordBreak: "break-word" }}>
              <strong>Collection name: </strong> {collectionNameValue}
              <Typography variant="body1">
              <strong>Policy ID:</strong> {policyId}
              <br />
              <strong>NFTs associated with policy:</strong> {nftCount}
              </Typography>
            </Typography>
            <Button
              variant="outlined"
              color="error"
              size="medium"
              onClick={handleDeletePolicy}
              startIcon={<Delete />}
              sx={{ mt: 1 }}
            >
              Delete Policy
            </Button>
          </Box>
        ) : (
          <Box sx={{ display: "flex", flexDirection: "column", gap: "8px", pb: 2 }}>
            <Typography variant="h7" sx={{ mb: 3, mt: 3, fontWeight: "bold" }}>
              No policy is currently set.
            </Typography>
            {/* Require collection name and locking period before enabling create policy */}
            <Controller
              control={control}
              name="collectionName"
              render={({ field }) => (
                <FormControl fullWidth sx={{ mb: 2 }}>
                  <FormLabel>Collection Name</FormLabel>
                  <TextField
                    {...field}
                    placeholder="My Awesome NFT Collection"
                    InputProps={{ readOnly: false }}
                  />
                  <FormHelperText>
                    The name of your NFT collection.
                  </FormHelperText>
                </FormControl>
              )}
            />
            <FormControl fullWidth sx={{ mb: 2 }}>
              <FormLabel>Policy locking period (epochs)</FormLabel>
              <Controller
                control={control}
                name="policyLockEpoch"
                render={({ field }) => (
                  <TextField
                    {...field}
                    type="number"
                    placeholder="Enter epoch number"
                    InputProps={{ readOnly: false, min: 1 }}
                  />
                )}
              />
              <FormHelperText>
                This defines the epoch after which the policy will be locked.
              </FormHelperText>
                {policyError && (
                  <FormHelperText error>{policyError}</FormHelperText>
                )}

            </FormControl>
            <Button
              variant="outlined"
              color="error"
              size="medium"
              onClick={() => {
                  // Manual validation for collectionName and policyLockEpoch
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
              sx={{ mt: 1 }}
              disabled={
                !collectionNameValue ||
                !policyLockEpochValue
              }
            >
              Create Policy
            </Button>
          </Box>
        )}

        <Divider />

        {policyId ? (
          <Box sx={{ display: "flex", flexDirection: "column", gap: "16px" }}>
            <Typography variant="h6">NFT Metadata</Typography>
            <Controller
              control={control}
              name="nftMetadata"
              render={({ field }) => (
                <FormControl fullWidth error={!!errors.nftMetadata}>
                  <FormLabel>NFT Metadata (JSON)</FormLabel>
                  <Box
                    sx={{
                      fontSize: 14,
                      whiteSpace: "pre-wrap",
                      fontFamily: "monospace",
                      backgroundColor: theme.palette.background.paper,
                      borderRadius: 1,
                      padding: 2,
                      minHeight: 200,
                      maxHeight: 300,
                      overflow: "auto",
                      border: `1px solid ${theme.palette.divider}`,
                    }}
                    data-color-mode="light"
                  >
                    {(metadataExists) ? (
                      <pre style={{ margin: 0 }}>{field.value}</pre>
                    ) : (
                      <CodeEditor
                        value={field.value}
                        onChange={(e) => field.onChange(e.target.value)}
                        highlight={(code) => highlight(code, languages.json, "json")}
                        padding={10}
                        style={{
                          fontSize: 14,
                          fontFamily: "monospace",
                          backgroundColor: theme.palette.background.paper,
                          borderRadius: 4,
                          minHeight: 200,
                          maxHeight: 300,
                          width: "100%",
                          border: `1px solid ${theme.palette.divider}`,
                          outline: "none",
                          whiteSpace: "pre-wrap",
                          overflow: "auto",
                        }}
                      />
                    )}
                  </Box>
                  <FormHelperText>
                    JSON format metadata for your NFT collection. Once metadata is uploaded it's not changeable.
                    <br />
                    If you want to change it, you need to delete it and upload the new one. If metadata is deleted and reuploaded, the mint will start from 0.
                  </FormHelperText>
                    {metadataError && (
                      <FormHelperText error>{metadataError}</FormHelperText>
                    )}

                </FormControl>
              )}
            />

            {metadataExists ?(
              <Button
                variant="outlined"
                color="warning"
                size="medium"
                onClick={handleDeleteMetadata}
                startIcon={<Delete />}
                sx={{ mt: 1 }}
              >
                Delete Metadata
              </Button>
            ) : (
              <Button
                variant="outlined"
                color="primary"
                size="medium"
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
                sx={{ mt: 1 }}
                disabled={
                  !control._formValues.nftMetadata
                }
              >
                Upload Metadata
              </Button>
            )}
          </Box>
        ) : null}

        <Divider />

        {isEditingParams ? (

          <form
          style={{ display: "flex", flexDirection: "column", gap: "24px" }}
          onSubmit={handleSubmit(saveParams)}
        >
          <Box sx={{ display: "flex", flexDirection: "column", gap: "16px" }}>
            <Typography variant="h6">Minting Parameters</Typography>
            <Box
              sx={{
                display: "grid",
                gridTemplateColumns: { xs: "1fr", md: "1fr 1fr" },
                gap: "16px",
              }}
            >
              <Controller
                control={control}
                name="nftPrice"
                render={({ field }) => (
                  <FormControl fullWidth error={!!errors.nftPrice}>
                    <FormLabel>NFT Price (Lovelace)</FormLabel>
                    <TextField
                      {...field}
                      type="number"
                      error={!!errors.nftPrice}
                      InputProps={{ readOnly: false, inputProps: { min: 1000000 } }}
                    />
                    <FormHelperText>
                      The price in Lovelace required to mint one NFT (1 ADA =
                      1,000,000 Lovelace).
                    </FormHelperText>
                    {errors.nftPrice && (
                      <FormHelperText error>
                        {errors.nftPrice.message}
                      </FormHelperText>
                    )}
                  </FormControl>
                )}
              />

              <Controller
                control={control}
                name="collectionSize"
                render={() => (
                  <FormControl fullWidth >
                    <FormLabel>Collection Size</FormLabel>
                    <TextField
                      value={nftCount}
                      type="number"
                      disabled
                    />
                    <FormHelperText>
                      The total number of NFTs in the collection. Collection size is the amount of NFT metadata entries that have been uploaded.
                    </FormHelperText>
                  </FormControl>
                )}
              />
            </Box>

            <Box
              sx={{
                display: "grid",
                gridTemplateColumns: { xs: "1fr", md: "1fr 1fr" },
                gap: "16px",
              }}
            >
              <Controller
                control={control}
                name="nftsReservedPerTx"
                render={({ field }) => (
                  <FormControl fullWidth error={!!errors.nftsReservedPerTx}>
                    <FormLabel>NFTs Reserved Per Transaction</FormLabel>
                    <TextField
                      {...field}
                      type="number"
                      error={!!errors.nftsReservedPerTx}
                      InputProps={{ readOnly: false, inputProps: { min: 1 } }}
                    />
                    <FormHelperText>
                      Maximum number of NFTs that can be reserved in a single
                      transaction.
                    </FormHelperText>
                    {errors.nftsReservedPerTx && (
                      <FormHelperText error>
                        {errors.nftsReservedPerTx.message}
                      </FormHelperText>
                    )}
                  </FormControl>
                )}
              />

              <Controller
                control={control}
                name="nftsToMintPerTx"
                render={({ field }) => (
                  <FormControl fullWidth error={!!errors.nftsToMintPerTx}>
                    <FormLabel>NFTs to Mint Per Transaction</FormLabel>
                    <TextField
                      {...field}
                      type="number"
                      error={!!errors.nftsToMintPerTx}
                      InputProps={{ readOnly: false, inputProps: { min: 1 } }}
                    />
                    <FormHelperText>
                      Maximum number of NFTs that can be minted in a single
                      transaction.
                    </FormHelperText>
                    {errors.nftsToMintPerTx && (
                      <FormHelperText error>
                        {errors.nftsToMintPerTx.message}
                      </FormHelperText>
                    )}
                  </FormControl>
                )}
              />
            </Box>

            <Box
              sx={{
                display: "grid",
                gridTemplateColumns: { xs: "1fr", md: "1fr 1fr" },
                gap: "16px",
              }}
            >
              <Controller
                control={control}
                name="nftsToNotMint"
                render={({ field }) => (
                  <FormControl fullWidth error={!!errors.nftsToNotMint}>
                    <FormLabel>NFTs to Not Mint</FormLabel>
                    <TextField
                      {...field}
                      type="number"
                      error={!!errors.nftsToNotMint}
                      InputProps={{ readOnly: false, inputProps: { min: 0 } }}
                    />
                    <FormHelperText>
                      Number of NFTs in the collection that should not be
                      minted.
                    </FormHelperText>
                    {errors.nftsToNotMint && (
                      <FormHelperText error>
                        {errors.nftsToNotMint.message}
                      </FormHelperText>
                    )}
                  </FormControl>
                )}
              />

              <Controller
                control={control}
                name="refundsPerTxLimit"
                render={({ field }) => (
                  <FormControl fullWidth error={!!errors.refundsPerTxLimit}>
                    <FormLabel>Refunds Per Transaction Limit</FormLabel>
                    <TextField
                      {...field}
                      type="number"
                      error={!!errors.refundsPerTxLimit}
                      InputProps={{ readOnly: false, inputProps: { min: 1 } }}
                    />
                    <FormHelperText>
                      Maximum number of refunds allowed per transaction.
                    </FormHelperText>
                    {errors.refundsPerTxLimit && (
                      <FormHelperText error>
                        {errors.refundsPerTxLimit.message}
                      </FormHelperText>
                    )}
                  </FormControl>
                )}
              />
            </Box>
          </Box>

          {saveSuccess === false && (
            <Alert severity="error" sx={{ mt: 2 }}>
              Failed to save parameters. Please try again.
            </Alert>
          )}

          {saveSuccess === true && (
            <Alert severity="success" sx={{ mt: 2 }}>
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
              variant="outlined"
              startIcon={<Save />}
              color="primary"
              sx={{ mt: 1, width: "100%" }}
              size="medium"
            >
              Save Parameters
            </Button>

          </CardActions>
        </form>

        ) : (

            <Box sx={{ display: "flex", flexDirection: "column", gap: "16px" }}>
            <Typography variant="h6">Minting Parameters</Typography>
            <Box
              sx={{
                display: "grid",
                gridTemplateColumns: { xs: "1fr", md: "1fr 1fr" },
                gap: "16px",
              }}
            >
              <FormControl fullWidth>
                <FormLabel>NFT Price (Lovelace)</FormLabel>
                <TextField
                  value={nftPrice}
                  type="number"
                  InputProps={{ readOnly: true }}
                />
                <FormHelperText>
                  The price in Lovelace required to mint one NFT (1 ADA =
                  1,000,000 Lovelace).
                </FormHelperText>
              </FormControl>
              <FormControl fullWidth>
                <FormLabel>Collection Size</FormLabel>
                <TextField
                  value={nftCount}
                  type="number"
                  disabled
                />
                <FormHelperText>
                  The total number of NFTs in the collection. Collection size is the amount of NFT metadata entries that have been uploaded.
                </FormHelperText>
              </FormControl>
            </Box>
            <Box
              sx={{
                display: "grid",
                gridTemplateColumns: { xs: "1fr", md: "1fr 1fr" },
                gap: "16px",
              }}
            >
              <FormControl fullWidth>
                <FormLabel>NFTs Reserved Per Transaction</FormLabel>
                <TextField
                  value={nftsReservedPerTx}
                  type="number"
                  InputProps={{ readOnly: true }}
                />
                <FormHelperText>
                  Maximum number of NFTs that can be reserved in a single
                  transaction.
                </FormHelperText>
              </FormControl>
              <FormControl fullWidth>
                <FormLabel>NFTs to Mint Per Transaction</FormLabel>
                <TextField
                  value={nftsToMintPerTx}
                  type="number"
                  InputProps={{ readOnly: true }}
                />
                <FormHelperText>
                  Maximum number of NFTs that can be minted in a single
                  transaction.
                </FormHelperText>
              </FormControl>
            </Box>
            <Box
              sx={{
                display: "grid",
                gridTemplateColumns: { xs: "1fr", md: "1fr 1fr" },
                gap: "16px",
              }}
            >
              <FormControl fullWidth>
                <FormLabel>NFTs to Not Mint</FormLabel>
                <TextField
                  value={nftsToNotMint}
                  type="number"
                  InputProps={{ readOnly: true }}
                />
                <FormHelperText>
                  Number of NFTs in the collection that should not be
                  minted.
                </FormHelperText>
              </FormControl>
              <FormControl fullWidth>
                <FormLabel>Refunds Per Transaction Limit</FormLabel>
                <TextField
                  value={refundsPerTxLimit}
                  type="number"
                  InputProps={{ readOnly: true }}
                />
                <FormHelperText>
                  Maximum number of refunds allowed per transaction.
                </FormHelperText>
              </FormControl>
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
                color="error"
                sx={{ mt: 1, width: "100%" }}
                size="medium"
              >
                Edit Parameters
              </Button>
            </CardActions>
          </Box>
        )}


      </CardContent>
    </Card>
  );
};

export default ConfigurationPanel;