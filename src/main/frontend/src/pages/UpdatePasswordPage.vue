<script setup lang="ts">
import {ref} from 'vue';
import { QForm } from 'quasar';
import { email } from 'src/composables/EmailComposable.ts'
import {GenericResponseDto} from 'components/dto/GenericResponseDto.ts';
import { useRouteQuery } from '@vueuse/router';
import {useRouter} from 'vue-router';

defineOptions({
  name: 'UpdatePasswordPage'
});

const router = useRouter();
const token = useRouteQuery('token', '', {transform: String});

const { setNewPassword } = email();

const formData = ref({
  password: '',
  passwordRepeated: ''
});

const confirmation = ref<GenericResponseDto>({
  message: 'not changed',
  error: ''
});

const formRef = ref<QForm>();
const confirm = ref<boolean>(false);

const sendToken = async () => {
  confirmation.value = await setNewPassword(token.value.toString(), formData.value.password, formData.value.passwordRepeated);

  if(confirmation.value.error == 'no'){
    await router.push('/login');
  }
};

</script>

<template>
  <q-layout>
    <q-page-container>
      <q-page>

        <q-card class="outer-card-style">

          <q-card-section>
            <div class="text-h6">Reset password</div>
            <div class="text-subtitle2">Set a new password</div>
          </q-card-section>

          <q-card-section class="q-pa-md">
            <q-form ref="formRef" @submit="sendToken">

              <q-input
                v-model="formData.password"
                label="Enter your new password"
                filled
                style="width: 50%;"
                type="textarea"
                bg-color="white"
                name="itemDescription"
              />

              <q-input
                v-model="formData.passwordRepeated"
                label="Enter your new password again"
                filled
                style="width: 50%;"
                type="textarea"
                bg-color="white"
                name="itemDescription"
              />

              <div class="row justify-center" style="padding-top: 10px;">
                <q-btn label="Submit"
                       type="submit"
                       color="primary"
                       class="glossy"
                       @click="confirm = true"
                />
              </div>
            </q-form>
          </q-card-section>
        </q-card>

        <q-dialog v-model="confirm">
          <q-card>

            <q-card-section class="q-pt-none">
                {{ confirmation.message }}
            </q-card-section>

            <q-card-actions align="right">
              <q-btn flat label="OK" color="primary" v-close-popup />
            </q-card-actions>
          </q-card>
        </q-dialog>

      </q-page>
    </q-page-container>
  </q-layout>

</template>

<style lang="scss" scoped>
@import 'src/css/quasar.variables.scss';

.outer-card-style {
  @include outer-card-style;
}

</style>
