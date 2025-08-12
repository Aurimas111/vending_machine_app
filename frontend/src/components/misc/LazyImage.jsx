
import React, { useState, useEffect } from "react";
import { Box } from "@mui/material";
import { useTheme, alpha } from "@mui/material/styles";
import CircularProgress from "@mui/material/CircularProgress";
import { Skeleton } from "@mui/material";


// Lazy loading hook
const useLazyImage = (src, options = {}) => {
  const [imageSrc, setImageSrc] = useState(null);
  const [imageRef, setImageRef] = useState();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    let observer;
    
    if (imageRef && imageSrc !== src) {
      if (IntersectionObserver) {
        observer = new IntersectionObserver(
          entries => {
            entries.forEach(entry => {
              if (entry.isIntersecting) {
                setLoading(true);
                setError(false);
                setImageSrc(src);
                observer.unobserve(imageRef);
              }
            });
          },
          {
            threshold: 0.1,
            rootMargin: "100px", // Start loading 100px before coming into view
            ...options
          }
        );
        observer.observe(imageRef);
      } else {
        setImageSrc(src);
      }
    }
    
    return () => {
      if (observer && observer.unobserve && imageRef) {
        observer.unobserve(imageRef);
      }
    };
  }, [src, imageRef, imageSrc]);

  return { setImageRef, imageSrc, loading, error, setLoading, setError };
};

const LazyImage = ({ 
  src, 
  alt, 
  sx = {}, 
  onLoad,
  onError,
  ...props 
}) => {
  const theme = useTheme();
  const { setImageRef, imageSrc, loading, setLoading, setError } = useLazyImage(src);
  const [imgLoaded, setImgLoaded] = useState(false);

  const handleLoad = () => {
    setLoading(false);
    setImgLoaded(true);
    onLoad?.();
  };

  const handleError = () => {
    setError(true);
    setLoading(false);
    onError?.();
  };

  return (
    <Box
      ref={setImageRef}
      sx={{
        position: 'relative',
        overflow: 'hidden',
        bgcolor: 'action.hover',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        ...sx
      }}
      {...props}
    >
      {/* Loading skeleton */}
      {loading && !imgLoaded && !imageSrc && (
        <Skeleton 
          variant="rectangular" 
          width="100%" 
          height="100%"
          sx={{ position: 'absolute', inset: 0 }}
        />
      )}
      
      {/* Loading spinner when image is being fetched */}
      {imageSrc && loading && !imgLoaded && (
        <Box sx={{ 
          position: 'absolute', 
          inset: 0, 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center',
          bgcolor: alpha(theme.palette.background.paper, 0.8),
          zIndex: 1
        }}>
          <CircularProgress size={24} />
        </Box>
      )}
      
      {imageSrc && (
        <Box
          component="img"
          src={imageSrc}
          alt={alt}
          onLoad={handleLoad}
          onError={handleError}
          sx={{
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            opacity: imgLoaded ? 1 : 0,
            transition: 'opacity 0.3s ease',
          }}
        />
      )}
      
      {/* Fallback placeholder */}
      {!imageSrc && !loading && (
        <Box sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          height: '100%',
          color: 'text.disabled'
        }}>
          <ImageIcon sx={{ fontSize: 48 }} />
        </Box>
      )}
    </Box>
  );
};

export default LazyImage;