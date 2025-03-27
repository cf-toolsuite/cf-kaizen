package org.cftoolsuite.cfapp.service.ai;

import org.apache.commons.lang3.StringUtils;
import org.cftoolsuite.cfapp.butler.api.ProductsApiClient;
import org.cftoolsuite.cfapp.butler.model.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductsService {

    private final ProductsApiClient productsApiClient;

    public ProductsService(ProductsApiClient productsApiClient) {
        this.productsApiClient = productsApiClient;
    }

    @Tool(name = "ProductGetDeployedProducts", description =
            """
            Retrieves a comprehensive list of all currently deployed products from Operations Manager.
            Use this tool when you need verify what products are installed.
            """
    )
    public List<DeployedProduct> getDeployedProducts() {
        return productsApiClient.productsDeployedGet().getBody();
    }

    @Tool(name = "ProductGetDeployedProductInsights", description =
            """
            Provides detailed insights on deployed products in Operations Manager, including:
            - Complete buildpacks inventory with versions and update status
            - Stemcell details including OS version and compatibility
            - Installed tiles with their release information and metadata
            Each component includes corresponding metadata from Tanzu Network for comprehensive analysis.
            Use this when you need in-depth information about the deployment environment.
            """
    )
    public ProductMetrics getProductMetrics() {
        return productsApiClient.productsMetricsGet().getBody();
    }

    @Tool(name = "ProductGetOperationsManagerVersion", description =
            """
            Retrieves the specific version of Operations Manager currently running.
            Use this when troubleshooting compatibility issues or planning upgrades.
            """
    )
    public OmInfo getOmInfo() {
        return productsApiClient.productsOmInfoGet().getBody();
    }


    @Tool(name = "ProductGetStemcellAssociations", description =
            """
            Retrieves detailed stemcell associations from Operations Manager (when the version detected is 2.6 or later)
            Maps the relationships between products and their required stemcells.
            Use this when planning stemcell upgrades or analyzing cross-dependencies.
            """
    )
    public StemcellAssociations getStemcellAssociations() {
        return productsApiClient.productsStemcellAssociationsGet().getBody();
    }

    @Tool(name = "ProductGetProductInfoByName", description =
            """
            Searches for and retrieves detailed product information from Tanzu Network by name pattern.
            Provides minimal details about the matched product including name, slug, and documentation links.
            Source: https://developer.broadcom.com/xapis/tanzu-api/latest/api/v2/products/get/
            Specify a clear name pattern for more accurate results.
            """
    )
    public Product getProductByName(@ToolParam (description = "Product name pattern to search for (case-insensitive)") String namePattern) {
        return Optional.ofNullable(productsApiClient.storeProductCatalogGet())
                .map(HttpEntity::getBody)
                .map(Products::getProducts)
                .flatMap(products -> products.stream()
                        .filter(product -> StringUtils.isNotBlank(product.getName()) &&
                                product.getName().toLowerCase().contains(namePattern.toLowerCase()))
                        .findFirst())
                .orElse(null);
    }

    @Tool(name = "ProductGetLatestProductReleaseBySlug", description =
            """
            Retrieves the absolute latest release of a product by its slug identifier from Tanzu Network.
            Returns comprehensive details about the latest release including version, release date, and download links.
            Source: https://developer.broadcom.com/xapis/tanzu-api/latest/api/v2/products/product_slug/releases/get/
            Use this when you need the most current release information for a specific product.
            """
    )
    public Release getLatestProductReleaseBySlug(@ToolParam (description = "Product slug identifier pattern (case-insensitive)") String slugPattern) {
        return Optional.ofNullable(productsApiClient.storeProductReleasesGet("latest"))
                .map(HttpEntity::getBody)
                .flatMap(releases -> releases.stream()
                        .filter(release -> StringUtils.isNotBlank(release.getSlug()) &&
                                release.getSlug().toLowerCase().contains(slugPattern.toLowerCase()))
                        .findFirst())
                .orElse(null);
    }
}