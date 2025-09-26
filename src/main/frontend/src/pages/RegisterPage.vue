<script setup lang="ts">
import { ref } from 'vue';
import { RegisterUser } from 'components/dto/RegisterUser.ts';
import axios from 'axios';

const user = ref<RegisterUser>( {
  username: '',
  name: '',
  email: '',
  password: '',
  passwordRepeated: '',
});

const api = axios.create({
  baseURL: '/',
  headers: { 'Content-Type': 'application/json', },
  maxRedirects: 0
});

const sendUpdate = async () => {
  await api.post<RegisterUser>('/registration', user.value)
    .then(response => {
        if (response.headers['Content-Type'] !== 'application/json') {
          window.location.href = response.headers['Location']
        }
        console.log(response);
  })
    .catch(error => {
        console.log(error);
        popupMessage.value = true;
    })
};

const popupMessage = ref<boolean>(false);

defineOptions({
  name: 'RegisterPage'
});
</script>

<template>
  <q-layout>
    <q-page-container>
      <q-page class="flex flex-center">
        <q-page class="row items-center justify-evenly">

          <q-form v-model="user" @submit="sendUpdate">

            <q-icon name="info">
              <q-tooltip class="text-body1">
                The username you want to use, for other people to find you by.
              </q-tooltip>
            </q-icon>
            <q-input v-model="user.username" label="Username" />

            <q-icon name="info">
              <q-tooltip class="text-body1">
                Your real name, only shown to your close friends.
              </q-tooltip>
            </q-icon>
            <q-input v-model="user.name" label="Name" />

            <q-icon name="info">
              <q-tooltip class="text-body1">
                Your email address.
              </q-tooltip>
            </q-icon>
            <q-input v-model="user.email" label="Email" />

            <q-icon name="info">
              <q-tooltip class="text-body1">
                Your desired password.
                Password must have at least one small letter,<br>
                at least one capital letter,<br>
                at least one digit,<br>
                at least one special symbol,<br>
                and be between 8 to 20 characters.<br>
              </q-tooltip>
            </q-icon>

            <q-input v-model="user.password" type="password" label="Password" />

            <q-icon name="info">
              <q-tooltip class="text-body1">
                Your desired password a second time, to make sure you didn't make any mistakes.
              </q-tooltip>
            </q-icon>
            <q-input v-model="user.passwordRepeated" type="password" label="Password again" />
            <div>
              <q-btn label="Submit" type="submit" color="primary"/>
            </div>

          </q-form>

          <q-dialog v-model="popupMessage">
            <q-card>

              <q-card-section class="q-pt-none">
                One of the sections didn't meet format requirements. <br>
                Please adjust and try again.
              </q-card-section>

              <q-card-actions align="right">
                <q-btn flat label="Close" color="primary" v-close-popup />
              </q-card-actions>
            </q-card>
          </q-dialog>

        </q-page>
      </q-page>
    </q-page-container>
  </q-layout>
</template>
