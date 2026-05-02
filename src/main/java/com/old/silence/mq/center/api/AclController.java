package com.old.silence.mq.center.api;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.AclConfig;
import org.apache.rocketmq.common.PlainAccessConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.common.base.Preconditions;
import com.old.silence.mq.center.api.config.RMQConfigure;
import com.old.silence.mq.center.domain.service.AclService;
import com.old.silence.mq.center.dto.AclRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1/acl")
public class AclController {

    private static final Logger logger = LoggerFactory.getLogger(AclController.class);

    private final AclService aclService;

    private final RMQConfigure configure;

    public AclController(AclService aclService, RMQConfigure configure) {
        this.aclService = aclService;
        this.configure = configure;
    }

    @GetMapping("/enable")
    public Boolean isEnableAcl() {
        try {
            Boolean result = configure.isACLEnabled();
            logger.info("ACL status check: {}", result ? "ENABLED" : "DISABLED");
            return result;
        } catch (Exception e) {
            logger.error("Failed to check ACL status", e);
            return false;
        }
    }

    @GetMapping("/config")
    public AclConfig getAclConfig() {
        try {
            AclConfig config = aclService.getAclConfig(false);
            int userCount = config.getPlainAccessConfigs() != null ? config.getPlainAccessConfigs().size() : 0;
            int whiteAddrCount = config.getGlobalWhiteAddrs() != null ? config.getGlobalWhiteAddrs().size() : 0;
            logger.info("Retrieved ACL config: {} users, {} white addresses", userCount, whiteAddrCount);
            return config;
        } catch (Exception e) {
            logger.error("Failed to retrieve ACL configuration from broker", e);
            return new AclConfig();
        }
    }

    @PostMapping("/add")
    public Boolean addAclConfig(@RequestBody PlainAccessConfig config) {
        try {
            Preconditions.checkArgument(StringUtils.isNotEmpty(config.getAccessKey()), "accessKey is null");
            Preconditions.checkArgument(StringUtils.isNotEmpty(config.getSecretKey()), "secretKey is null");

            logger.info("Adding new ACL user: key={}, admin={}", config.getAccessKey(), config.isAdmin());
            aclService.addAclConfig(config);
            logger.info("ACL user created successfully: {}", config.getAccessKey());
            return true;
        } catch (IllegalArgumentException e) {
            logger.warn("ACL config validation failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Failed to create ACL user [{}]", config != null ? config.getAccessKey() : "unknown", e);
            return false;
        }
    }

    @DeleteMapping("/delete")
    public Boolean deleteAclConfig(@RequestBody PlainAccessConfig config) {
        try {
            Preconditions.checkArgument(StringUtils.isNotEmpty(config.getAccessKey()), "accessKey is null");
            String accessKey = config.getAccessKey();
            logger.info("Removing ACL user: {}", accessKey);
            try {
                aclService.deleteAclConfig(config);
                logger.info("ACL user removed: {}", accessKey);
            } catch (Exception e) {
                logger.warn("Failed to remove ACL user [{}] from broker: {}", accessKey, e.getMessage());
            }
            return true;
        } catch (IllegalArgumentException e) {
            logger.warn("Missing required parameter: {}", e.getMessage());
            return false;
        }
    }

    @PostMapping("/update")
    public Boolean updateAclConfig(@RequestBody PlainAccessConfig config) {
        try {
            Preconditions.checkArgument(StringUtils.isNotEmpty(config.getSecretKey()), "secretKey is null");
            String accessKey = config.getAccessKey();
            logger.info("Updating ACL user: key={}, admin={}", accessKey, config.isAdmin());
            try {
                aclService.updateAclConfig(config);
                logger.info("ACL user updated: {}", accessKey);
            } catch (Exception e) {
                logger.warn("Failed to update ACL user [{}]: {}", accessKey, e.getMessage());
            }
            return true;
        } catch (IllegalArgumentException e) {
            logger.warn("Parameter validation failed: {}", e.getMessage());
            return false;
        }
    }

    @PostMapping("/topic/add")
    public Boolean addAclTopicConfig(@RequestBody AclRequest request) {
        try {
            Preconditions.checkArgument(StringUtils.isNotEmpty(request.getConfig().getAccessKey()), "accessKey is null");
            Preconditions.checkArgument(StringUtils.isNotEmpty(request.getConfig().getSecretKey()), "secretKey is null");
            Preconditions.checkArgument(CollectionUtils.isNotEmpty(request.getConfig().getTopicPerms()), "topic perms is null");
            Preconditions.checkArgument(StringUtils.isNotEmpty(request.getTopicPerm()), "topic perm is null");

            String accessKey = request.getConfig().getAccessKey();
            String topicPerm = request.getTopicPerm();
            int topicCount = request.getConfig().getTopicPerms().size();
            logger.info("Granting topic permission: user={}, topics={}, permission={}", accessKey, topicCount, topicPerm);
            try {
                aclService.addOrUpdateAclTopicConfig(request);
                logger.info("Topic permission granted: {}@{}", accessKey, topicPerm);
            } catch (Exception e) {
                logger.warn("Failed to grant topic permission [{}@{}]: {}", accessKey, topicPerm, e.getMessage());
            }
            return true;
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: {}", e.getMessage());
            return false;
        }
    }

    @PostMapping("/group/add")
    public Boolean addAclGroupConfig(@RequestBody AclRequest request) {
        try {
            Preconditions.checkArgument(StringUtils.isNotEmpty(request.getConfig().getAccessKey()), "accessKey is null");
            Preconditions.checkArgument(StringUtils.isNotEmpty(request.getConfig().getSecretKey()), "secretKey is null");
            Preconditions.checkArgument(CollectionUtils.isNotEmpty(request.getConfig().getGroupPerms()), "group perms is null");
            Preconditions.checkArgument(StringUtils.isNotEmpty(request.getGroupPerm()), "group perm is null");

            String accessKey = request.getConfig().getAccessKey();
            String groupPerm = request.getGroupPerm();
            int groupCount = request.getConfig().getGroupPerms().size();
            logger.info("Granting consumer group permission: user={}, groups={}, permission={}", accessKey, groupCount, groupPerm);
            try {
                aclService.addOrUpdateAclGroupConfig(request);
                logger.info("Consumer group permission granted: {}@{}", accessKey, groupPerm);
            } catch (Exception e) {
                logger.warn("Failed to grant consumer group permission [{}@{}]: {}", accessKey, groupPerm, e.getMessage());
            }
            return true;
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: {}", e.getMessage());
            return false;
        }
    }

    @PostMapping("/perm/delete")
    public Boolean deletePermConfig(@RequestBody AclRequest request) {
        try {
            Preconditions.checkArgument(StringUtils.isNotEmpty(request.getConfig().getAccessKey()), "accessKey is null");
            Preconditions.checkArgument(StringUtils.isNotEmpty(request.getConfig().getSecretKey()), "secretKey is null");

            String accessKey = request.getConfig().getAccessKey();
            logger.info("Revoking all permissions: {}", accessKey);
            try {
                aclService.deletePermConfig(request);
                logger.info("All permissions revoked: {}", accessKey);
            } catch (Exception e) {
                logger.warn("Failed to revoke permissions for [{}]: {}", accessKey, e.getMessage());
            }
            return true;
        } catch (IllegalArgumentException e) {
            logger.warn("Parameter validation failed: {}", e.getMessage());
            return false;
        }
    }

    @PostMapping("/sync")
    public Boolean syncConfig(@RequestBody PlainAccessConfig config) {
        try {
            Preconditions.checkArgument(StringUtils.isNotEmpty(config.getAccessKey()), "accessKey is null");
            Preconditions.checkArgument(StringUtils.isNotEmpty(config.getSecretKey()), "secretKey is null");

            String accessKey = config.getAccessKey();
            logger.info("Syncing ACL data to all brokers: {}", accessKey);
            try {
                aclService.syncData(config);
                logger.info("ACL data synchronized across cluster: {}", accessKey);
            } catch (Exception e) {
                logger.warn("Failed to sync ACL data for [{}]: {}", accessKey, e.getMessage());
            }
            return true;
        } catch (IllegalArgumentException e) {
            logger.warn("Parameter validation failed: {}", e.getMessage());
            return false;
        }
    }

    @PostMapping("/white/list/add")
    public Boolean addWhiteList(@RequestBody List<String> whiteList) {
        try {
            Preconditions.checkArgument(CollectionUtils.isNotEmpty(whiteList), "white list is null");

            logger.info("Adding {} addresses to white list: {}", whiteList.size(), whiteList);
            try {
                aclService.addWhiteList(whiteList);
                logger.info("{} addresses added to white list", whiteList.size());
            } catch (Exception e) {
                logger.warn("Failed to add white list entries: {}", e.getMessage());
            }
            return true;
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: {}", e.getMessage());
            return false;
        }
    }

    @DeleteMapping("/white/list/delete")
    public Boolean deleteWhiteAddr(@RequestParam String request) {
        try {
            logger.info("Removing white list entry: {}", request);
            try {
                aclService.deleteWhiteAddr(request);
                logger.info("White list entry removed: {}", request);
            } catch (Exception e) {
                logger.warn("Failed to remove white list entry [{}]: {}", request, e.getMessage());
            }
            return true;
        } catch (Exception e) {
            logger.error("Error processing white list deletion for [{}]", request, e);
            return false;
        }
    }

    @PostMapping("/white/list/sync")
    public Boolean synchronizeWhiteList(@RequestBody List<String> whiteList) {
        try {
            Preconditions.checkArgument(CollectionUtils.isNotEmpty(whiteList), "white list is null");

            logger.info("Synchronizing white list across cluster with {} addresses", whiteList.size());
            try {
                aclService.synchronizeWhiteList(whiteList);
                logger.info("White list synchronized successfully: {} addresses", whiteList.size());
            } catch (Exception e) {
                logger.warn("White list synchronization incomplete: {}", e.getMessage());
            }
            return true;
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: {}", e.getMessage());
            return false;
        }
    }
}
