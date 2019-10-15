#include <jni.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <string>
#include <android/log.h>
#include "torch/script.h"

extern "C" {

JNIEXPORT jstring JNICALL
Java_ark_testapp_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT void JNICALL Java_ark_testapp_MainActivity_native_1load(JNIEnv* env, jobject,
        jobject Amgr, jstring modeldir) {
    AAssetManager *mgr = AAssetManager_fromJava(env, Amgr);
    AAssetDir* assetDir = AAssetManager_openDir(mgr, "model");
    const char* filename = (const char*)NULL;
    size_t BUFSZ = 1000 * 512;
    const char* rootdir = (env)->GetStringUTFChars(modeldir, 0);
    while ((filename = AAssetDir_getNextFileName(assetDir)) != NULL) {
        char fin[7 + strlen(filename)];
        strcpy(fin, "model/");
        strcat(fin, filename);

        char fout[1 + strlen(rootdir) + strlen(filename)];
        strcpy(fout, rootdir);
        strcat(fout, filename);
        int nb_read = 0;
        char* buf = new char[BUFSZ];

        AAsset* file = AAssetManager_open(mgr, fin, AASSET_MODE_STREAMING);
        FILE* out = fopen(fout, "w");
        if (out == NULL) {
            __android_log_print(ANDROID_LOG_INFO, "Bla", "%s", "FUCKED");
        }
        while ((nb_read = AAsset_read(file, buf, BUFSZ)) > 0) {
            fwrite(buf, nb_read, 1, out);
        }
        delete[] buf;
        fclose(out);
        AAsset_close(file);
    }
    AAssetDir_close(assetDir);
}

int run_model(std::string modeldir) {
    torch::jit::script::Module module;
    module = torch::jit::load(modeldir + "traced_model.pt");

    std::vector<torch::jit::IValue> inputs;
    inputs.push_back(torch::ones({1, 3}));
    torch::autograd::AutoGradMode guard(false);
    at::Tensor output = module.forward(inputs).toTensor();
    return 0;
}

JNIEXPORT jint JNICALL
Java_ark_testapp_MainActivity_runPytorch(JNIEnv* env, jobject, jstring jmodeldir) {
    const char* cstr = env->GetStringUTFChars(jmodeldir, NULL);
    std::string modeldir = std::string(cstr);
    int res = run_model(modeldir);
    return res;
}

}