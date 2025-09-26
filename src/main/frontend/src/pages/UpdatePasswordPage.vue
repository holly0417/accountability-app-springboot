<script setup lang="ts">
import {reactive, ref} from 'vue';
import { QForm } from 'quasar';
import { email } from 'src/composables/EmailComposable.ts'
import { useRouteQuery } from '@vueuse/router';
import {useRouter} from 'vue-router';
import {ResetPasswordDto} from 'components/dto/ResetPasswordDto.ts';

defineOptions({
  name: 'UpdatePasswordPage'
});

const router = useRouter();
const token = useRouteQuery('token', '', {transform: String});

const { setNewPassword, error } = email();

const fieldErrors = reactive<Record<string, string>>({
  password: '',
  confirmPassword: '',
  token: ''
});

const formData = ref<ResetPasswordDto>({
  password: '',
  passwordRepeated: '',
  token: ''
})

const toLoginPage = async () => {
  await router.push('/login');
}

const formRef = ref<QForm>();
const confirmation = ref<boolean>(false);

const sendToken = async () => {
  // Clear previous errors
  Object.keys(fieldErrors).forEach(key => fieldErrors[key] = '');
  formData.value.token = token.value;

  try {

    await setNewPassword(formData.value)
    confirmation.value = true;

  } catch (err) {

    if (error.value?.errors) {
      Object.entries(error.value.errors).forEach(([field, message]) => {
        if (field in fieldErrors) {
          fieldErrors[field] = message as string;
        }
      });
    }

    console.error('Registration failed:', error.value?.message);
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
              <div>
                <q-input
                  v-model="formData.password"
                  label="Enter your new password"
                  filled
                  style="width: 50%;"
                  type="password"
                  :class="{ 'error': fieldErrors.password }"
                  bg-color="white"
                  name="itemDescription"
                />
                <span class="error-message">{{ fieldErrors.password }}</span>
              </div>

              <div>
                <q-input
                  v-model="formData.passwordRepeated"
                  label="Confirm new password"
                  filled
                  style="width: 50%;"
                  type="password"
                  :class="{ 'error': fieldErrors.confirmPassword }"
                  bg-color="white"
                  name="itemDescription"
                />
                <span class="error-message">{{ fieldErrors.confirmPassword }}</span>
              </div>

              <div v-if="error && !error.errors" class="general-error">
                {{ error.message }}
              </div>

              <div class="row justify-center" style="padding-top: 10px;">
                <q-btn label="Submit"
                       type="submit"
                       color="primary"
                       class="glossy"
                />
              </div>

            </q-form>
          </q-card-section>
        </q-card>

        <q-dialog v-model="confirmation">
            <q-card>
              <q-card-section class="q-pt-none">
                Password successfully changed!
              </q-card-section>
              <q-card-actions align="right">
                <q-btn @click="toLoginPage" flat label="OK" color="primary" v-close-popup />
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
