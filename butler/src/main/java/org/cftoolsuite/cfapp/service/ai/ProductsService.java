package org.cftoolsuite.cfapp.service.ai;

import org.cftoolsuite.cfapp.butler.api.ProductsApiClient;
import org.cftoolsuite.cfapp.butler.model.DeployedProduct;
import org.cftoolsuite.cfapp.butler.model.OmInfo;
import org.cftoolsuite.cfapp.butler.model.ProductMetrics;
import org.cftoolsuite.cfapp.butler.model.Products;
import org.cftoolsuite.cfapp.butler.model.Release;
import org.cftoolsuite.cfapp.butler.model.StemcellAssignments;
import org.cftoolsuite.cfapp.butler.model.StemcellAssociations;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductsService {

    private final ProductsApiClient productsApiClient;

    public ProductsService(ProductsApiClient productsApiClient) {
        this.productsApiClient = productsApiClient;
    }

    @Tool(description = "Get deployed products from Operations Manager.")
    public List<DeployedProduct> getDeployedProducts() {
        return productsApiClient.productsDeployedGet().getBody();
    }

    @Tool(description =
        """
            Assembles lists of deployed products from Operations Manager.
            Lists include: buildpacks, stemcells, and tiles.
            Each entry in each list is enhanced with release metadata from Tanzu Network.
        """)
    public ProductMetrics getProductMetrics() {
        return productsApiClient.productsMetricsGet().getBody();
    }

    @Tool(description = "Get the version of Operations Manager.")
    public OmInfo getOmInfo() {
        return productsApiClient.productsOmInfoGet().getBody();
    }

    @Tool(description = "Get stemcell assignments from Operations Manager.")
    public StemcellAssignments getStemcellAssignments() {
        return productsApiClient.productsStemcellAssignmentsGet().getBody();
    }

    @Tool(description = "Get stemcell associations from Operations Manager (v2.6+).")
    public StemcellAssociations getStemcellAssociations() {
        return productsApiClient.productsStemcellAssociationsGet().getBody();
    }

    @Tool(description = "Get product catalog from Tanzu Network.  Source: https://developer.broadcom.com/xapis/tanzu-api/latest/api/v2/products/get/.")
    public Products getProductCatalog() {
        return productsApiClient.storeProductCatalogGet().getBody();
    }

    @Tool(description = "Get product releases from Tanzu Network.  Source: https://developer.broadcom.com/xapis/tanzu-api/latest/api/v2/products/product_slug/releases/get/.")
    public List<Release> getProductReleases(@ToolParam(description = "Query option (latest, all, recent).") String q) {
        return productsApiClient.storeProductReleasesGet(q).getBody();
    }
}