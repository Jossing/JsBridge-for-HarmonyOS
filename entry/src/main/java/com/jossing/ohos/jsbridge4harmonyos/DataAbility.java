package com.jossing.ohos.jsbridge4harmonyos;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.global.resource.RawFileDescriptor;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.PacMap;
import ohos.utils.net.Uri;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DataAbility extends Ability {
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(HiLog.LOG_APP, 0x65536, "DataAbility");

    private static final String PLACEHOLDER_RAW_FILE = "/rawfile/";
    private static final String ENTRY_PATH_PREFIX = "entry/resources";

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        HiLog.info(LABEL_LOG, "DataAbility onStart");
    }

    @Override
    public RawFileDescriptor openRawFile(Uri uri, String mode) throws FileNotFoundException {
        final int splitChar = '/';
        if (uri == null) {
            throw new FileNotFoundException("Invalid Uri");
        }

        // path will be like /com.example.dataability/rawfile/example.html
        final String path = uri.getEncodedPath();
        final int splitIndex = path.indexOf(splitChar, 1);
        if (splitIndex < 0) {
            throw new FileNotFoundException("Invalid Uri " + uri);
        }

        final String targetPath = path.substring(splitIndex);
        if (targetPath.startsWith(PLACEHOLDER_RAW_FILE)) {
            // 根据自定义规则访问资源文件
            try {
                return getResourceManager().getRawFileEntry(ENTRY_PATH_PREFIX + targetPath).openRawFileDescriptor();
            } catch (IOException e) {
                throw new FileNotFoundException("Not found support raw file at " + uri);
            }
        } else {
            throw new FileNotFoundException("Not found support file at " + uri);
        }
    }

    @Override
    public ResultSet query(Uri uri, String[] columns, DataAbilityPredicates predicates) {
        return null;
    }

    @Override
    public int insert(Uri uri, ValuesBucket value) {
        HiLog.info(LABEL_LOG, "DataAbility insert");
        return -1;
    }

    @Override
    public int delete(Uri uri, DataAbilityPredicates predicates) {
        return 0;
    }

    @Override
    public int update(Uri uri, ValuesBucket value, DataAbilityPredicates predicates) {
        return 0;
    }

    @Override
    public FileDescriptor openFile(Uri uri, String mode) {
        return null;
    }

    @Override
    public String[] getFileTypes(Uri uri, String mimeTypeFilter) {
        return new String[0];
    }

    @Override
    public PacMap call(String method, String arg, PacMap extras) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
}