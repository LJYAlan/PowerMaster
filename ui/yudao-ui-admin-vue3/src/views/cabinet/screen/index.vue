<template>
  <ContentWrap>
    <div class="screenContiner">
      <div class="deviceList">
        <div v-for="item in deviceRight" :key="item.id" class="device">
          <div class="name">设备名称： {{item.rackName}}</div>
          <div class="info">
            <div>型号：{{item.type}}</div>
            <div>占用：{{item.uHeight}}</div>
          </div>
        </div>
      </div>
      <div class="machine">
        <div class="mainBorder">
          <div class="main">
            <template v-for="(item, index) in frameList" :key="index">
              <div v-if="item.uHeight > 0" class="Uitem active" :style="`min-height: ${height}`">{{item.rackName}}</div>
              <div v-else class="Uitem"></div>
            </template>
          </div>
        </div>
        <div class="base"></div>
      </div>
      <div class="deviceList">
        <div v-for="item in deviceLeft" :key="item.id" class="device">
          <div class="name">设备名称： {{item.rackName}}</div>
          <div class="info">
            <div>型号：{{item.type}}</div>
            <div>占用：{{item.uHeight}}</div>
          </div>
        </div>
      </div>
    </div>
    <div class="local">{{cabinetInfo.roomName}}-{{cabinetInfo.cabinetName}}</div>
    <div class="infomation">
      <div class="infoItem">
        <span class="num">{{cabinetInfo.cabinetHeight}}</span>
        <span>空间总容量</span>
      </div>
      <div class="line"></div>
      <div class="infoItem">
        <span class="num">{{cabinetInfo.usedSpace}}U</span>
        <span>已用空间</span>
      </div>
      <div class="line"></div>
      <div class="infoItem">
        <span class="num">{{cabinetInfo.freeSpace}}U</span>
        <span>未用空间</span>
      </div>
      <div class="line"></div>
      <div class="infoItem">
        <span class="num">{{cabinetInfo.rackNum}}</span>
        <span>设备总数</span>
      </div>
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { CabinetApi } from '@/api/cabinet/info'

const cabinetInfo = ref({})
const deviceLeft = ref([])
const deviceRight = ref([])
const frameList = ref([])
const height = ref('0px')
const cabinetId = history?.state?.id || 1
console.log('cabinetId', cabinetId)

const getData = async() => {
  const res = await CabinetApi.getCabinetInfoItem({id: cabinetId})
  console.log('res', res)
  cabinetInfo.value = res
  if (res.rackIndexList && res.rackIndexList.length > 0) {
    deviceLeft.value = res.rackIndexList.filter((item,index) => index%2 == 0)
    deviceRight.value = res.rackIndexList.filter((item,index) => index%2 == 1)
    const frames = [] as any
    for(let i = 1; i <= res.cabinetHeight; i++) {
      frames.push({})
    }
    res.rackIndexList.forEach(item => {
      frames.splice(item.uAddress-1, item.uHeight, item)
    })
    frameList.value = frames.reverse()
    console.log('frames', frames)
    if (res.rackIndexList.length < 11) {
      height.value = '30px'
    } else if (res.rackIndexList.length.length < 16) {
      height.value = '25px'
    } else if (res.rackIndexList.length.length < 21) {
      height.value = '20px'
    } else if (res.rackIndexList.length.length < 26) {
      height.value = '15px'
    }
  }
}

getData()
</script>

<style lang="scss" scoped>
.screenContiner {
  position: relative;
  display: flex;
  justify-content: space-evenly;
  .deviceList {
    height: 588px;
    overflow: auto;
    &::-webkit-scrollbar {
      width: 0;
      height: 0;
    }
    .device {
      width: 310px;
      height: 130px;
      margin-bottom: 15px;
      font-size: 14px;
      border: 1px solid #eee;
      .name {
        padding: 10px 8px;
        background-color: #eee;
        // background: linear-gradient(to bottom right, #000c4248, #0004ff);
        
      }
      .info {
        padding: 0 8px;
        div {
          margin-top: 15px;
        }
      }
    }
  }
  .machine {
    position: relative;
    width: 260px;
    height: fit-content;
    box-sizing: border-box;
    &::before {
      content: "";
      position: absolute;
      bottom: -15px; /* 控制梯形的上方边长 */
      left: 20px;
      width: 12px;
      height: 0;
      border-bottom: 15px solid #90b8df; /* 控制梯形的底边长度和颜色 */
      border-left: 10px solid transparent; /* 控制梯形的左侧斜边 */
      border-right: 10px solid transparent; /* 控制梯形的右侧斜边 */
      transform: rotateX(180deg)
    }
    &::after {
      content: "";
      position: absolute;
      bottom: -15px; /* 控制梯形的上方边长 */
      right: 20px;
      width: 12px;
      height: 0;
      border-bottom: 15px solid #90b8df; /* 控制梯形的底边长度和颜色 */
      border-left: 10px solid transparent; /* 控制梯形的左侧斜边 */
      border-right: 10px solid transparent; /* 控制梯形的右侧斜边 */
      transform: rotateX(180deg)
    }
    .mainBorder {
      // height: calc(100% - 20px);
      background-color: #90b8df;
      box-sizing: border-box;
      padding: 18px 18px 40px 18px;
      .main {
        height: 500px;
        display: flex;
        flex-direction: column;
        width: 100%;
        // height: 100%;
        box-sizing: border-box;
        border: 5px solid;
        background-color: #fff;
        .Uitem {
          flex: 1;
          width: 100%;
          background-color: #fff;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #eee;
          font-size: 14px;
        }
        .active {
          min-height: 20px;
          height: 30px;
          background-color: #5298df;
          border-radius: 5px;
          margin-top: 1px;
          margin-bottom: 1px;
        }
      }
    }
    .base {
      height: 20px;
      background-color: #51677c;
    }
  }
}
.local {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 15px;
}
.infomation {
  display: flex;
  justify-content: center;
  align-items: center;
  .infoItem {
    padding: 20px 30px;
    display: flex;
    flex-direction: column;
    // justify-content: center;
    align-items: center;
    font-size: 13px;
    .num {
      font-size: 16px;
      margin-bottom: 5px;
    }
  }
  .line {
    width: 1px;
    height: 20px;
    background-color: #eee;
  }
}
</style>