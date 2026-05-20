

<template>
  <main>
    <div style = "font-weight:bold">
      嘻嘻
    </div>

    <div style="color:#333;font-size:32px;font-weight: bold">
      <span v-if="data.a > 10">
        {{a}}  {{data.a}} {{data.b}}
      </span>
    </div>
    <input v-model = "data.a" style = "margin-bottom: 20px" />


    <div style = "display:flex">
      <div @click="show(item)" style = "width: 200px; height: 200px; text-align: center; line-height: 200px; background-color: dodgerblue ; margin-right: 10px ; margin-bottom: 20px" v-for = "item in arr"> {{item}} </div>

    </div>

    <div style = "margin-bottom: 20px">
      <select>
        <option v-for = "item2 in arr">
          {{item2}}
        </option>
      </select>
    </div>

    <div style="margin-bottom: 20px">
      <el-button type="info" @click = "click" :icon="Delete" circle />
    </div>

    <div style = "margin-bottom: 20px">
      <img :src ="data.img" alt="">
    </div>

    <div style = "margin-bottom: 20px">
      <el-button>Default</el-button>
      <el-button type="info" round>Info</el-button>
    </div>

    <div>
      <el-icon>
        <span><ZoomIn /></span>
      </el-icon>
    </div>

    <div style="margin-bottom: 20px">
      <RouterLink to="/test">跳转到测试界面</RouterLink>
    </div>

    <div style = "margin-bottom: 20px">
      <button @click = r></button>
    </div>

  </main>
</template>


<script setup>
import { RouterLink } from 'vue-router'
import {ref} from "vue";
import {reactive} from "vue";
import {ZoomIn} from "@element-plus/icons-vue";
import {Delete} from "@element-plus/icons-vue";


const a = ref(123)
const data = reactive({
  a:10,
  b:true,
  img:'https://image.baidu.com/search/detail?ct=503316480&z=0&tn=baiduimagedetail&ipn=d&cl=2&cm=1&sc=0&sa=vs_ala_img_datu&lm=-1&ie=utf8&pn=0&rn=1&di=7523999300557209601&ln=0&word=%E5%8E%9F%E7%A5%9E%E5%9B%BE%E7%89%87&os=3732448696%2C3273726665&cs=2980547343%2C3505290908&objurl=http%3A%2F%2Ffastcdn.mihoyo.com%2Fstatic-resource-v2%2F2023%2F10%2F19%2F6d0282f379fe02999660555b7f5879bc_2021256533866225005.jpg&bdtype=0&simid=4235135207%2C699163057&pi=0&adpicid=0&timingneed=&spn=0&is=0%2C0&lid=d3a759f9000152e1',
  d:4
})
const arr = [9,2,3]
const click = () =>{
  alert("嘻嘻嘻")
}
const show = (value) =>{
  alert(value)
}

const my_style = reactive({
  box:{
    width:'200px',
    height:'200px',

  }
})

</script>