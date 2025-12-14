package com.maxinhai.platform.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.maxinhai.platform.dto.OrderAddDTO;
import com.maxinhai.platform.enums.OrderStatus;
import com.maxinhai.platform.feign.SystemFeignClient;
import com.maxinhai.platform.handler.HashHandler;
import com.maxinhai.platform.handler.ListHandler;
import com.maxinhai.platform.handler.StringHandler;
import com.maxinhai.platform.mapper.TaskOrderMapper;
import com.maxinhai.platform.mapper.UserMapper;
import com.maxinhai.platform.mapper.WorkOrderMapper;
import com.maxinhai.platform.po.Order;
import com.maxinhai.platform.po.TaskOrder;
import com.maxinhai.platform.po.User;
import com.maxinhai.platform.po.WorkOrder;
import com.maxinhai.platform.service.SimulateUserActionService;
import com.maxinhai.platform.utils.AjaxResult;
import com.maxinhai.platform.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @ClassName：SimulateUserActionServiceImpl
 * @Author: XinHai.Ma
 * @Date: 2025/10/24 22:15
 * @Description: 模拟用户动作业务层（当抖音关注用户直播时，登录用户账户，执行订单创建、派工单开工、派工单暂停、派工单复工、派工单报工操作）
 */
@Slf4j
@Service
public class SimulateUserActionServiceImpl implements SimulateUserActionService {

    @Resource
    private StringHandler stringHandler;
    @Resource
    private ListHandler listHandler;
    @Resource
    private HashHandler hashHandler;
    @Resource
    private SystemFeignClient systemFeign;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private TaskOrderMapper taskOrderMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    @Qualifier("ioIntensiveExecutor")
    public Executor ioIntensiveExecutor;
    @Value("${spring.profiles.active}")
    private String env;
    @Resource
    private RestTemplate restTemplate;

    /**
     * 抖音关注正在直播账号
     **/
    private static final String DOUYIN_LIVE_LIST = "douyin:live:list";
    /**
     * 在线用户
     **/
    private static final String ONLINE_USER_LIST = "douyin:online:list";

    @PostConstruct
    public void loadToken() {
        List<User> userList = userMapper.selectList(new LambdaQueryWrapper<User>().select(User::getAccount));
        for (User user : userList) {
            String account = user.getAccount();
            Object token = stringHandler.get("auth:token:" + account);
            if (token != null) {
                userTokenMap.put(account, token.toString());
            }
        }
        log.info("加载用户token完毕");
    }

    @Scheduled(initialDelay = 1000, fixedRate = 60000)
    public void updateDuplicateAccount() {
        int row = userMapper.updateUserPassword();
        log.info("更新用户默认密码 => 影响行数: {}", row);

        List<User> userList = userMapper.selectUserListDuplicateAccount();
        Map<String, List<User>> userMap = userList.stream().collect(Collectors.groupingBy(User::getAccount));
        userMap.entrySet().forEach(entry -> {
            String account = entry.getKey();
            List<User> duplicateUserList = entry.getValue();
            for (int i = 1; i < duplicateUserList.size(); i++) {
                User user = duplicateUserList.get(i);
                LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(User::getAccount, user.getAccount() + i);
                updateWrapper.eq(User::getId, user.getId());
                userMapper.update(updateWrapper);
            }
            log.info("处理重复账号数据 => 账号: {}, 数据量: {}", account, duplicateUserList.size());
        });
    }

    /**
     * 在线直播账号 -> 更新账号在线状态 -> 账号活跃记录 -> 模拟用户动作
     */

    @Override
    @Scheduled(initialDelay = 5000, fixedRate = 3000)
    public void getLiveSteamingUser() {
        Map<String, String> headers = new HashMap<>();
        String cookie = "enter_pc_once=1; UIFID_TEMP=28ea90c1b0cf804752225259882c701fb12323f08ef828fc1032b615e29efbbea06dc38c89faf9df392d4bd20a6e390645a731204708193bbdd23f4349c026fb59720c5db1bb4a1ae643f5b0553e6c46; hevc_supported=true; dy_swidth=1920; dy_sheight=1080; fpk1=U2FsdGVkX19vjvEtvh4IlyCaxOUeaVblEpRvSlAW2U4lI2K1I0FfOQSp4byxdKvj8SJaRukmvTzywA8qtRJ3+g==; fpk2=0e0369e2813db7deb26e5937c353aab4; s_v_web_id=verify_me448dy7_Tfsde2Dm_6GEq_4kA7_9NzQ_6CmLstNqPFgr; xgplayer_device_id=20656154195; xgplayer_user_id=604362628617; UIFID=28ea90c1b0cf804752225259882c701fb12323f08ef828fc1032b615e29efbbea06dc38c89faf9df392d4bd20a6e390698aaaf09c1da46c04218de191c80d221fe532392b4606414a0b2a493d09caec3c4d1b6344fa95803422be3dacef30f6349974c9812a0ce31f1ac29d99073933c892d41e32c5db9100f1bceb96b565e86bd2318171bd7379a6c67555beb193bc8759d00e02597b116af2722fdbb933fcf; is_dash_user=1; __security_mc_1_s_sdk_crypt_sdk=9bcebcc0-4356-883b; bd_ticket_guard_client_web_domain=2; passport_csrf_token=87ab3b391ef393f7537f0253dd6416c3; passport_csrf_token_default=87ab3b391ef393f7537f0253dd6416c3; d_ticket=afc14ce956f0df67d5084fa8568757c8ee8d0; n_mh=imZMMIWwPRng1tRBBpq52CaKAFQRTJGogxg7dP1uyAA; uid_tt=fdb85ce2161b28f4cbe7ea93e669e031; uid_tt_ss=fdb85ce2161b28f4cbe7ea93e669e031; sid_tt=80e60b1699453db025097dcf54008303; sessionid=80e60b1699453db025097dcf54008303; sessionid_ss=80e60b1699453db025097dcf54008303; is_staff_user=false; __security_mc_1_s_sdk_cert_key=7bb277c8-432a-b3d1; __security_server_data_status=1; live_use_vvc=%22false%22; h265ErrorNum=-1; passport_mfa_token=CjcD4a%2BkCCbXTPTw9%2B621UJUz8QLYvmk3HVwql6SryKbmLlWD%2BMbnmR7rlxlE0%2B2bF5vghLtcZVRGkoKPAAAAAAAAAAAAABPVnjfMC0PRuiQFcIoKotjXdRycWHrj4r5Qj0E2uibC3nUVLV9jHP%2FlPLC2Iduj5R8CxDsjfkNGPax0WwgAiIBA4x%2BXP8%3D; SEARCH_RESULT_LIST_TYPE=%22single%22; __druidClientInfo=JTdCJTIyY2xpZW50V2lkdGglMjIlM0EzMDQlMkMlMjJjbGllbnRIZWlnaHQlMjIlM0E2ODUlMkMlMjJ3aWR0aCUyMiUzQTMwNCUyQyUyMmhlaWdodCUyMiUzQTY4NSUyQyUyMmRldmljZVBpeGVsUmF0aW8lMjIlM0ExJTJDJTIydXNlckFnZW50JTIyJTNBJTIyTW96aWxsYSUyRjUuMCUyMChXaW5kb3dzJTIwTlQlMjAxMC4wJTNCJTIwV2luNjQlM0IlMjB4NjQpJTIwQXBwbGVXZWJLaXQlMkY1MzcuMzYlMjAoS0hUTUwlMkMlMjBsaWtlJTIwR2Vja28pJTIwQ2hyb21lJTJGMTM2LjAuMC4wJTIwU2FmYXJpJTJGNTM3LjM2JTIyJTdE; theme=%22light%22; shareRecommendGuideTagCount=3; my_rd=2; download_guide=%220%2F%2F1%22; publish_badge_show_info=%221%2C0%2C0%2C1757500613284%22; passport_assist_user=CkGeDtPI1nBUNesvHD8mRDFqGXcWBnTvYI1Iq5t-BpvLyUEcpT6Ubb20C93_izX0m_Ed07NwHk5DYQVCczZP3XPTIBpKCjwAAAAAAAAAAAAAT3VlXIqY9TWAGl3W57keNN3mEJz0f1RFpe4XrPA-Ife_ZkYAcYyg51BxKsneEApxQRQQ9-f7DRiJr9ZUIAEiAQP6NSYd; sid_guard=80e60b1699453db025097dcf54008303%7C1757511794%7C5184000%7CSun%2C+09-Nov-2025+13%3A43%3A14+GMT; sid_ucp_v1=1.0.0-KDA2ODM3MDUxOTE1OTdjY2JlYzIwODFlN2MxMjFlMjI2MTYzZGIyODQKIQjNuLDtpoyHBBDygIbGBhjvMSAMMLC1m5kGOAVA-wdIBBoCbGYiIDgwZTYwYjE2OTk0NTNkYjAyNTA5N2RjZjU0MDA4MzAz; ssid_ucp_v1=1.0.0-KDA2ODM3MDUxOTE1OTdjY2JlYzIwODFlN2MxMjFlMjI2MTYzZGIyODQKIQjNuLDtpoyHBBDygIbGBhjvMSAMMLC1m5kGOAVA-wdIBBoCbGYiIDgwZTYwYjE2OTk0NTNkYjAyNTA5N2RjZjU0MDA4MzAz; login_time=1757511793202; __security_mc_1_s_sdk_sign_data_key_web_protect=165812a2-426d-b78a; _bd_ticket_crypt_cookie=7b147e12024e5bee736bd7bd34f98cf2; __ac_nonce=068c2addc0007cdb041a6; __ac_signature=_02B4Z6wo00f01ccwlzAAAIDAGGtkfbodfo3HEJOAABkQae; douyin.com; xg_device_score=7.818598560905933; device_web_cpu_core=20; device_web_memory_size=8; architecture=amd64; home_can_add_dy_2_desktop=%220%22; stream_recommend_feed_params=%22%7B%5C%22cookie_enabled%5C%22%3Atrue%2C%5C%22screen_width%5C%22%3A1920%2C%5C%22screen_height%5C%22%3A1080%2C%5C%22browser_online%5C%22%3Atrue%2C%5C%22cpu_core_num%5C%22%3A20%2C%5C%22device_memory%5C%22%3A8%2C%5C%22downlink%5C%22%3A10%2C%5C%22effective_type%5C%22%3A%5C%224g%5C%22%2C%5C%22round_trip_time%5C%22%3A50%7D%22; SelfTabRedDotControl=%5B%7B%22id%22%3A%227321354226083301387%22%2C%22u%22%3A313%2C%22c%22%3A309%7D%2C%7B%22id%22%3A%227527666382473791527%22%2C%22u%22%3A14%2C%22c%22%3A14%7D%5D; strategyABtestKey=%221757588961.033%22; __live_version__=%221.1.3.9904%22; webcast_local_quality=null; live_can_add_dy_2_desktop=%221%22; bd_ticket_guard_client_data=eyJiZC10aWNrZXQtZ3VhcmQtdmVyc2lvbiI6MiwiYmQtdGlja2V0LWd1YXJkLWl0ZXJhdGlvbi12ZXJzaW9uIjoxLCJiZC10aWNrZXQtZ3VhcmQtcmVlLXB1YmxpYy1rZXkiOiJCS0tSd2hpcll3VXExU0JnVTlGTEQ3T2tiNXRkQXpmQkRCVVk1aUs2OHJPaXZPUlVJNEgvcmFTekhRRmw3SnArOGJqY0tqWXNDRzBOUi83OWlSSklnL3M9IiwiYmQtdGlja2V0LWd1YXJkLXdlYi12ZXJzaW9uIjoyfQ%3D%3D; volume_info=%7B%22isUserMute%22%3Afalse%2C%22isMute%22%3Afalse%2C%22volume%22%3A0.233%7D; gulu_source_res=eyJwX2luIjoiMWNiYzdlMGU3OTFhOTYxMzEyMWJjY2MwOTBiMTgxNzdiNzk5N2Q1MmU1YThhZWZjZDQ0NDI2ZDM5ODZkNmUxZCJ9; WallpaperGuide=%7B%22showTime%22%3A0%2C%22closeTime%22%3A0%2C%22showCount%22%3A0%2C%22cursor1%22%3A202%2C%22cursor2%22%3A72%2C%22hoverTime%22%3A1754879694406%7D; odin_tt=acb8f414c022a61a65e99b0266a729b0008687bc493d1abe0be2be9cafb663c3e8037af2b43e675cfc18c27d365a37b1d539c771a73a9aed8ee1869275e449199c6ae48359eb4b702a50a06dca252afe; FOLLOW_LIVE_POINT_INFO=%22MS4wLjABAAAAMutji20RI-BreK3_03pd35nDj5AhhEaaLPg7Bh5efyuTIOcDYIVUilE5nBrtifz5%2F1757606400000%2F0%2F0%2F1757590927434%22; FOLLOW_NUMBER_YELLOW_POINT_INFO=%22MS4wLjABAAAAMutji20RI-BreK3_03pd35nDj5AhhEaaLPg7Bh5efyuTIOcDYIVUilE5nBrtifz5%2F1757606400000%2F0%2F0%2F1757591527435%22; stream_player_status_params=%22%7B%5C%22is_auto_play%5C%22%3A0%2C%5C%22is_full_screen%5C%22%3A0%2C%5C%22is_full_webscreen%5C%22%3A0%2C%5C%22is_mute%5C%22%3A0%2C%5C%22is_speed%5C%22%3A1%2C%5C%22is_visible%5C%22%3A1%7D%22; IsDouyinActive=true; playRecommendGuideTagCount=1; totalRecommendGuideTagCount=18; biz_trace_id=4878cacf; ttwid=1%7CH8ClIisyQoLCSznqG7alYZdyu7I2uBLTYR8_l3FuQtA%7C1757590572%7C463c37a758ad69318ebe4db19e21a4aaac9b416c7504b42de7430c787e97f15c; sdk_source_info=7e276470716a68645a606960273f276364697660272927676c715a6d6069756077273f276364697660272927666d776a68605a607d71606b766c6a6b5a7666776c7571273f275e58272927666a6b766a69605a696c6061273f27636469766027292762696a6764695a7364776c6467696076273f275e582729277672715a646971273f2763646976602729277f6b5a666475273f2763646976602729276d6a6e5a6b6a716c273f2763646976602729276c6b6f5a7f6367273f27636469766027292771273f2733323d363230353c3032303234272927676c715a75776a716a666a69273f2763646976602778; bit_env=ZGbHmhZEgWNzk5_3AF_bVJn0RXyVuwhfmKEJYiv7hxv_HQHm1Td1EfIoW70nRuzxPCLELc8__-F84e49jlYuwCTQjnfsmoK-JGmSqFJX5o7f4dtG2AaIhfw_n6H0tShDZG1yPujCrKJBSiBeBsKZyp5Fj-8wPbP708UxSlwSMzYHLCz870ZXS7CdeIglTPFuGr8Zv87KEwyP9XGtAIBP39K6gXBcsui7KwfXQGOkpHtsj9atKqIHCgc6FoVMsNVyU2xtGCCMbblxvyt36TVE0z2eTc2hgLtYBftPzHKS7MlfrKPsUSw_qPG38zc07vaa5F8yZBr5MRhcC_-SgPDuSykvNGSFvHVxnqXW-wozzntinBjg71kxdUNlv7VRjVy15YTVw-gmzYC2X9C_vmR3xXUKAJXl8OCFoCnovR9pC3vTAkw_rqOtiYD63guxe3VxxokpByjPpXC29xHoxZ8bYYEPU-1rZM_OsWF2Fjdcc0GQ79j70OdvpSJx_QbUOmNi; passport_auth_mix_state=i3qaduh35xua3k7caln3tcwweyuvfrm2; session_tlb_tag=sttt%7C4%7CgOYLFplFPbAlCX3PVACDA__________J-U400AxJxpxr7aXwUh9gfMXXXLMX7Yn5dYfmdzars2M%3D";
        headers.put("cookie", cookie);
        HttpRequest get = HttpUtil.createGet("https://www.douyin.com/webcast/web/feed/follow/?device_platform=webapp"
                + "&aid=6383"
                + "&channel=channel_pc_web"
                + "&scene=aweme_pc_follow_top"
                + "&update_version_code=170400"
                + "&pc_client_type=1"
                + "&pc_libra_divert=Windows"
                + "&support_h265=1"
                + "&support_dash=1"
                + "&cpu_core_num=20"
                + "&version_code=170400"
                + "&version_name=17.4.0"
                + "&cookie_enabled=true"
                + "&screen_width=1920"
                + "&screen_height=1080"
                + "&browser_language=zh-CN"
                + "&browser_platform=Win32"
                + "&browser_name=Chrome"
                + "&browser_version=136.0.0.0"
                + "&browser_online=true"
                + "&engine_name=Blink"
                + "&engine_version=136.0.0.0"
                + "&os_name=Windows"
                + "&os_version=10"
                + "&device_memory=8"
                + "&platform=PC"
                + "&downlink=10"
                + "&effective_type=4g"
                + "&round_trip_time=100"
                + "&webid=7507164795549173260&uifid=28ea90c1b0cf804752225259882c701fb12323f08ef828fc1032b615e29efbbeb37ab276c248dcd9d70a89df8e19c1e1319e3109f485cce53e3dbcbcfe49d96de0c0eea6bb0320a4e7d7ecd6fcf7ba23c907a2da4918cc1bc4bd2e1ccf643951e0e8a1afd2bc9db4a8b18ae8771cf9f2bff67ae7f2803ff8d1823b03ff1f9bdba89caf914ab4444c8ba1d30eef9899ff5eeec1c60c5c9d1a361d1346f2ef386b" + "&msToken=IRZNgqjWJoWnfBdbrn4BOUcoyjWVLcvrDzDHLCbqHUJAcsbvLrZRXTYKnjfVtijJfayMaONcxoBLzGrkInW8DNk95yhagQG3gLIA15lb8L0dftP_mIcT7GP9c8dt9q783G6InZl_C8x9cXy8gOBpp_y9QU0Uvvj0M64vX-QNRBJ4oQ%3D%3D&a_bogus=dj45g76yxdWROVMtmOD3yRZlOeEMrT8yEPixbwQTHOYBT1FaTmP32ae8coFzK5sVSuZzkI-7Tf4AiVpcOtUiZKFkwmkDSk7j5t%2FCnwvLMHkfT4Jg7ND2CbSEqiTbUSGY8%2FIvE%2F651s0e2E5W9NChApQ7K%2FUnm5jdFr3tV%2Fuji9K4UW8jwn%2Fna3YkLh17&verifyFp=verify_maz2to5l_WJRNPLUq_nzbE_4le3_91vq_QDjkUiNkgZa1&fp=verify_maz2to5l_WJRNPLUq_nzbE_4le3_91vq_QDjkUiNkgZa1");
        HttpResponse execute = get.addHeaders(headers).timeout(3000).execute();
        JSONObject parseObj = JSONUtil.parseObj(execute.body());
        JSONArray jsonArray = parseObj.getJSONObject("data").getJSONArray("data");

        List<Object> msdList = new ArrayList<>(CollectionUtils.isEmpty(jsonArray) ? 10 : jsonArray.size());
        for (Object object : jsonArray) {
            JSONObject jsonObject = (JSONObject) object;
            JSONObject owner = jsonObject.getJSONObject("room").getJSONObject("owner");
            String owner_user_id_str = jsonObject.getJSONObject("room").getStr("owner_user_id_str");
            String title = jsonObject.getJSONObject("room").getStr("title");
            String id_str = owner.getStr("id_str");
            String nickname = owner.getStr("nickname");

            JSONObject msg = new JSONObject();
            msg.set("id_str", id_str);
            msg.set("nickname", nickname);
            msg.set("owner_user_id_str", owner_user_id_str);
            msg.set("title", title);
            msdList.add(msg.toString());
        }
        listHandler.leftPushAll(DOUYIN_LIVE_LIST, msdList);

//        try {
//            mqttHandler.sendMessage("emqx", "/douyin/online", objectMapper.writeValueAsString(msdList));
//        } catch (MqttException e) {
//            throw new BusinessException("mqtt调用发生错误：" + e.getMessage());
//        } catch (JsonProcessingException e) {
//            throw new BusinessException("JSON序列化发生错误：" + e.getMessage());
//        }
    }

    @Override
    @Scheduled(initialDelay = 3000, fixedRate = 3000)
    public void countUserActivityDuration() {
        // 更新在线用户状态
        // 提高弹出元素速度，减少超时发生
        List<Object> msgList = null;
        do {
            msgList = listHandler.batchRightPop(DOUYIN_LIVE_LIST, 100);
            Map<Object, Object> onlineUserStatusMap = new HashMap<>();
            for (Object msg : msgList) {
                JSONObject jsonObject = JSONUtil.parseObj(msg.toString());
                // 改为批量更新
                JSONObject info = buildOnlineStatus(true, jsonObject.getStr("id_str"));
                onlineUserStatusMap.put(jsonObject.getStr("id_str"), info);
            }
            if (!CollectionUtils.isEmpty(onlineUserStatusMap)) {
                hashHandler.setAll(ONLINE_USER_LIST, onlineUserStatusMap);
            }
        } while (!CollectionUtils.isEmpty(msgList));
    }

    @Scheduled(initialDelay = 3000, fixedRate = 3000)
    public void updateAllUserStatus() {
        // 更新在线、离线用户状态
        if (!hashHandler.hasKey(ONLINE_USER_LIST)) {
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        Map<Object, Object> hash = (Map<Object, Object>) hashHandler.getAll(ONLINE_USER_LIST);

        // 1. 提前计算时间阈值（避免重复计算）
        long timeThreshold = System.currentTimeMillis() - 1000;

        // 2. 流式处理：过滤有效条目并构建新Map（使用collect避免副作用）
        Map<Object, Object> onlineUserStatusMap = hash.entrySet().stream()
                // 仅过滤：value必须是JSONObject（排除非JSONObject的无效数据）
                .filter(entry -> entry.getValue() instanceof JSONObject)
                // 转换：提取ts并判断是否符合时间条件，保留所有有效条目（无论true/false）
                .map(entry -> {
                    JSONObject value = (JSONObject) entry.getValue();
                    Long ts = value.getLong("ts"); // 若可能为null，建议先判断：value.optLong("ts", -1)
                    boolean isWithinTime = ts != null && ts > timeThreshold;
                    // 返回键和时间条件结果（true/false都保留）
                    return new AbstractMap.SimpleEntry<>(entry.getKey(), isWithinTime);
                })
                // 收集：所有条目都保留，根据isWithinTime构建不同状态
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, // 键：原始account
                        // 值：根据isWithinTime构建对应状态（true=在线，false=离线等）
                        tempEntry -> {
                            boolean flag = tempEntry.getValue();
                            String account = tempEntry.getKey().toString();
                            return buildOnlineStatus(flag, account); // 假设该方法可根据flag生成不同状态
                        }));

        // 3. 写入Redis
        hashHandler.setAll(ONLINE_USER_LIST, onlineUserStatusMap);
    }

    @Override
    @Scheduled(initialDelay = 3000, fixedRate = 120000)
    public void simulateUserAction() {
        Map<Object, Object> hash = (Map<Object, Object>) hashHandler.getAll(ONLINE_USER_LIST);
        // 筛选出当前状态是在线的用户
        List<Map.Entry<Object, Object>> onlineUserList = hash.entrySet().stream().filter(entry -> {
            if (entry.getValue() instanceof Map) {
                JSONObject value = (JSONObject) entry.getValue();
                return value.getBool("online", Boolean.FALSE);
            } else {
                return false;
            }
        }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(onlineUserList)) {
            return;
        }

        for (Map.Entry<Object, Object> entry : onlineUserList) {
            Object account = entry.getKey();
            // 交给
            distribute(account.toString());
        }
    }

    // 在线用户token
    private static final ConcurrentHashMap<String, String> userTokenMap = new ConcurrentHashMap<>();
    // 在线用户->派工单
    private static final ConcurrentHashMap<String, String> userTaskMap = new ConcurrentHashMap<>();
    // 在线用户->流程步骤
    private static final ConcurrentHashMap<String, String> userStepMap = new ConcurrentHashMap<>();

    /**
     * 给在线用户派发派工单
     *
     * @param account 账号
     */
    public void distribute(String account) {
        boolean loginFlag = userTokenMap.containsKey(account);
        String orderStatus = userStepMap.getOrDefault(account, "wait"); // 订单状态：wait.待分配 start.待开工 pause.待暂停 resume.待复工 report.待报工 none.没有派工单分配
        // 登录账户
        if (!loginFlag) {
            AjaxResult result = login(account);
            if (result.getCode() != HttpStatus.OK.value()) {
                ioIntensiveExecutor.execute(() -> register(account));
                log.error("登录 账号:{} -> 调用结果: {}", account, result);
            } else {
                userTokenMap.put(account, result.getData().toString());
                orderStatus = "wait";
                userStepMap.put(account, orderStatus);
            }
        } else if (loginFlag && "wait".equals(orderStatus)) {
            // 指派派工单给在线用户
            List<TaskOrder> taskOrderList = queryCanStartWorkTaskList();
            if (CollectionUtils.isEmpty(taskOrderList)) {
                userStepMap.put(account, "none");
                return;
            }
            // 按sort顺序遍历派工单，找到第一个未被分配的
            boolean assigned = false;
            for (TaskOrder taskOrder : taskOrderList) {
                String taskOrderId = taskOrder.getId();
                Collection<String> values = userTaskMap.values();
                if (!values.contains(taskOrderId)) {
                    userTaskMap.put(account, taskOrderId);
                    switch (taskOrder.getStatus()) {
                        case INIT:
                            userStepMap.put(account, "start");
                            break;
                        case START:
                            userStepMap.put(account, "pause");
                            break;
                        case PAUSE:
                            userStepMap.put(account, "resume");
                            break;
                        case RESUME:
                            userStepMap.put(account, "report");
                            break;
                        default:
                            break;
                    }
                    // 找到第一个未分配的，立即终止循环，避免覆盖
                    assigned = true;
                    break;
                }
            }

            // 如果没有找到可分配的派工单
            if (!assigned) {
                userStepMap.put(account, "none");
            }
        } else if (loginFlag && "none".equals(orderStatus)) {
            // 创建订单
            handleJwtExpired(account, createOrder(account));
            userStepMap.put(account, "wait");
        } else if (loginFlag && "start".equals(orderStatus)) {
            // 开工
            handleJwtExpired(account, startWork(account, userTaskMap.get(account)));
            userStepMap.put(account, "pause");
        } else if (loginFlag && "pause".equals(orderStatus)) {
            // 停工
            handleJwtExpired(account, pauseWork(account, userTaskMap.get(account)));
            userStepMap.put(account, "resume");
        } else if (loginFlag && "resume".equals(orderStatus)) {
            // 复工
            handleJwtExpired(account, resumeWork(account, userTaskMap.get(account)));
            userStepMap.put(account, "report");
        } else if (loginFlag && "report".equals(orderStatus)) {
            // 报工
            handleJwtExpired(account, reportWork(account, userTaskMap.get(account)));
            userTaskMap.remove(account);
            userStepMap.put(account, "wait");
        } else {
            throw new RuntimeException("未知状态");
        }
        log.info("派发用户: {}, 派工单: {}, 状态: {}", account, userTaskMap.get(account), orderStatus);
    }

    /**
     * 处理token过期
     *
     * @param account    账号
     * @param ajaxResult
     */
    private void handleJwtExpired(String account, AjaxResult ajaxResult) {
        if (ajaxResult.getCode() == HttpStatus.UNAUTHORIZED.value()) {
            userTokenMap.remove(account);
        }
        if (ajaxResult.getCode() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
            userTokenMap.remove(account);
        }
    }

    /**
     * 查询未开工的派工单
     *
     * @return 未开工的派工单集合
     */
    public List<TaskOrder> getTaskOrder() {
        List<String> workOrderIds = getWorkOrderList();
        // 首道工序开工
        List<TaskOrder> taskOrderList = taskOrderMapper.selectJoinList(TaskOrder.class, new MPJLambdaWrapper<TaskOrder>()
                .innerJoin(WorkOrder.class, WorkOrder::getId, TaskOrder::getWorkOrderId)
                .innerJoin(Order.class, Order::getId, TaskOrder::getOrderId)
                // 字段别名
                .select(TaskOrder::getId, TaskOrder::getStatus, TaskOrder::getSort)
                // 查询条件
                .eq(TaskOrder::getStatus, OrderStatus.INIT)
                .in(WorkOrder::getId, workOrderIds)
                .orderByAsc(TaskOrder::getSort)
                .orderByAsc(WorkOrder::getId));
        return taskOrderList;
    }

    /**
     * 查询未开工的工单
     *
     * @return 未开工的工单ID集合
     */
    public List<String> getWorkOrderList() {
        List<WorkOrder> workOrderList = workOrderMapper.selectJoinList(WorkOrder.class, new MPJLambdaWrapper<WorkOrder>()
                .innerJoin(Order.class, Order::getId, WorkOrder::getOrderId)
                // 字段别名
                .select(WorkOrder::getId)
                // 查询条件
                .eq(WorkOrder::getOrderStatus, OrderStatus.INIT)
                .eq(Order::getOrderStatus, OrderStatus.INIT)
                .between(WorkOrder::getCreateTime, DateUtil.offsetDay(new Date(), -30), DateUtils.getEndTimeOfToday())
                .orderByAsc(WorkOrder::getWorkOrderCode)
                .last("limit 300"));
        return workOrderList.stream().map(WorkOrder::getId).collect(Collectors.toList());
    }

    public AjaxResult login(String account) {
        // 构建登录账号参数
        JSONObject param = new JSONObject();
        param.put("account", account);
        param.put("password", "123456");

        // 创建请求头对象
        HttpHeaders headers = new HttpHeaders();
        // 设置请求头（根据需要添加，例如Token、User-Agent等）
        headers.add("sa-token", "internal");
        // 封装请求头和请求参数（GET请求无请求体，可传null）
        HttpEntity<JSONObject> requestEntity = new HttpEntity<>(param, headers);
        AjaxResult ajaxResult = restTemplate.exchange("http://" + judgeEnv(env, "system") + ":10010/api/auth/login", HttpMethod.POST, requestEntity, AjaxResult.class).getBody();
        return ajaxResult;
    }

    public AjaxResult register(String account) {
        // 构建登录账号参数
        JSONObject param = new JSONObject();
        param.put("account", account);
        param.put("username", account);
        param.put("password", "123456");
        param.put("phone", "15735400536");

        String token = login("caocao").getData().toString();

        // 创建请求头对象
        HttpHeaders headers = new HttpHeaders();
        // 设置请求头（根据需要添加，例如Token、User-Agent等）
        headers.add("sa-token", "internal");
        headers.add("Authorization", token);
        // 封装请求头和请求参数（GET请求无请求体，可传null）
        HttpEntity<JSONObject> requestEntity = new HttpEntity<>(param, headers);
        AjaxResult ajaxResult = restTemplate.exchange("http://" + judgeEnv(env, "system") + ":10010/api/auth/register", HttpMethod.POST, requestEntity, AjaxResult.class).getBody();
        return ajaxResult;
    }

    /**
     * 创建订单
     */
    public AjaxResult createOrder(String account) {
        AjaxResult<List<String>> ajaxResult = systemFeign.generateCode("order", 1);
        if (ajaxResult.getCode() != HttpStatus.OK.value()) {
            log.error("生成订单编码出错: {}", ajaxResult.getMsg());
            return ajaxResult;
        }

        // 构建创建订单参数
        OrderAddDTO param = new OrderAddDTO();
        param.setOrderCode(ajaxResult.getData().get(0));
        param.setQty(100);
        param.setOrderType(1);
        param.setPlanBeginTime(new Date());
        param.setPlanEndTime(DateUtil.offsetDay(param.getPlanBeginTime(), 7));
        param.setProductId("1971886706000953346");
        param.setBomId("1971893744164794369");
        param.setRoutingId("1971916261298331650");

        // 创建请求头对象
        HttpHeaders headers = new HttpHeaders();
        // 设置请求头（根据需要添加，例如Token、User-Agent等）
        headers.add("sa-token", "internal");
        headers.add("Authorization", userTokenMap.get(account));
        // 封装请求头和请求参数（GET请求无请求体，可传null）
        HttpEntity<OrderAddDTO> requestEntity = new HttpEntity<>(param, headers);
        ajaxResult = restTemplate.exchange("http://" + judgeEnv(env, "produce") + ":10040/order/addOrder", HttpMethod.POST, requestEntity, AjaxResult.class).getBody();
        return ajaxResult;
    }

    /**
     * 派工单-开工
     */
    public AjaxResult startWork(String account, String taskOrderId) {
        // 创建请求头对象
        HttpHeaders headers = new HttpHeaders();
        // 设置请求头（根据需要添加，例如Token、User-Agent等）
        headers.add("sa-token", "internal");
        headers.add("Authorization", userTokenMap.get(account));
        // 封装请求头和请求参数（GET请求无请求体，可传null）
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        AjaxResult ajaxResult = restTemplate.exchange("http://" + judgeEnv(env, "produce") + ":10040/taskOrder/startWork/{taskOrderId}", HttpMethod.GET, requestEntity, AjaxResult.class, taskOrderId).getBody();
        return ajaxResult;
    }

    /**
     * 派工单-暂停
     */
    public AjaxResult pauseWork(String account, String taskOrderId) {
        // 创建请求头对象
        HttpHeaders headers = new HttpHeaders();
        // 设置请求头（根据需要添加，例如Token、User-Agent等）
        headers.add("sa-token", "internal");
        headers.add("Authorization", userTokenMap.get(account));
        // 封装请求头和请求参数（GET请求无请求体，可传null）
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        AjaxResult ajaxResult = restTemplate.exchange("http://" + judgeEnv(env, "produce") + ":10040/taskOrder/pauseWork/{taskOrderId}", HttpMethod.GET, requestEntity, AjaxResult.class, taskOrderId).getBody();
        return ajaxResult;
    }

    /**
     * 派工单-复工
     */
    public AjaxResult resumeWork(String account, String taskOrderId) {
        // 创建请求头对象
        HttpHeaders headers = new HttpHeaders();
        // 设置请求头（根据需要添加，例如Token、User-Agent等）
        headers.add("sa-token", "internal");
        headers.add("Authorization", userTokenMap.get(account));
        // 封装请求头和请求参数（GET请求无请求体，可传null）
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        AjaxResult ajaxResult = restTemplate.exchange("http://" + judgeEnv(env, "produce") + ":10040/taskOrder/resumeWork/{taskOrderId}", HttpMethod.GET, requestEntity, AjaxResult.class, taskOrderId).getBody();
        return ajaxResult;
    }

    /**
     * 派工单-报工
     */
    public AjaxResult reportWork(String account, String taskOrderId) {
        // 创建请求头对象
        HttpHeaders headers = new HttpHeaders();
        // 设置请求头（根据需要添加，例如Token、User-Agent等）
        headers.add("sa-token", "internal");
        headers.add("Authorization", userTokenMap.get(account));
        // 封装请求头和请求参数（GET请求无请求体，可传null）
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        AjaxResult ajaxResult = restTemplate.exchange("http://" + judgeEnv(env, "produce") + ":10040/taskOrder/reportWork/{taskOrderId}", HttpMethod.GET, requestEntity, AjaxResult.class, taskOrderId).getBody();
        return ajaxResult;
    }


    /**
     * 构建用户在线状态信息
     *
     * @param flag    在线标记：true.在线 false.离线
     * @param account 账号
     * @return
     */
    public JSONObject buildOnlineStatus(boolean flag, String account) {
        JSONObject info = new JSONObject();
        info.put("account", account);
        info.put("online", flag);
        info.put("ts", System.currentTimeMillis());
        return info;
    }

    /**
     * 更新用户在线状态
     *
     * @param flag    在线标记：true.在线 false.离线
     * @param account 账号
     */
    @Override
    //@Async("ioIntensiveExecutor")
    public void updateUserStatus(boolean flag, String account) {
        if (flag) {
            updateOnlineUser(account);
        } else {
            updateOfflineUser(account);
        }
    }

    @Override
    public void updateOnlineUser(String account) {
        JSONObject info = null;
        if (hashHandler.hasKey(ONLINE_USER_LIST, account)) {
            info = (JSONObject) hashHandler.get(ONLINE_USER_LIST, account);
        } else {
            info = new JSONObject();
            info.put("account", account);
        }
        info.put("online", true);
        info.put("ts", System.currentTimeMillis());
        hashHandler.set(ONLINE_USER_LIST, account, info);
    }

    @Override
    public void updateOfflineUser(String account) {
        JSONObject info = null;
        if (hashHandler.hasKey(ONLINE_USER_LIST, account)) {
            info = (JSONObject) hashHandler.get(ONLINE_USER_LIST, account);
        } else {
            info = new JSONObject();
            info.put("account", account);
        }
        info.put("online", false);
        info.put("ts", System.currentTimeMillis());
        hashHandler.set(ONLINE_USER_LIST, account, info);
    }

    /**
     * 查询工单下可以开工、暂停、复工、报工的派工单集合
     *
     * @return 可以开工、暂停、复工、报工的派工单集合
     */
    public List<TaskOrder> queryCanStartWorkTaskList() {
        return taskOrderMapper.queryCanStartTaskList();
    }

    /**
     * 查找工单下可以开工、暂停、复工、报工的派工单
     *
     * @param taskOrderList 派工单集合
     * @return 可以开工、暂停、复工、报工的派工单
     */
    public TaskOrder findCanStartTask(List<TaskOrder> taskOrderList) {
        if (CollectionUtils.isEmpty(taskOrderList)) {
            return null;
        }
        for (TaskOrder taskOrder : taskOrderList) {
            // 派工单不是报工状态，就继续交给用户操作
            if (!OrderStatus.REPORT.equals(taskOrder.getStatus())) {
                return taskOrder;
            }
        }
        return null;
    }

    /**
     * 根据环境判断使用localhost还是容器名
     *
     * @param env         环境
     * @param serviceName 服务名称
     * @return
     */
    public String judgeEnv(String env, String serviceName) {
        String container = null;
        switch (env) {
            case "dev":
                container = "localhost";
                break;
            case "prod":
                container = "cloud-platform-" + serviceName;
                break;
            default:
                throw new RuntimeException("Invalid env: " + env);
        }
        return container;
    }

}
