import type { Component } from 'vue'

export type HttpMethod =
  | 'GET'
  | 'POST'
  | 'PUT'
  | 'PATCH'
  | 'DELETE'
  | 'HEAD'
  | 'OPTIONS'
  | '*'
  | (string & {})

export type ItemType = 'menu' | 'button' | 'api'

export interface MenuPermission {
  tag: string
  type: ItemType
  labelKey: string
  permissionKey?: string
  pattern?: string
  method?: HttpMethod | null
  isWhiteList?: boolean
}

export interface MenuItem {
  tag: string
  path?: string
  type: 'menu'
  labelKey: string
  icon?: Component
  leaf: boolean
  permissionKey?: string
  isWhiteList?: boolean
  hidden?: boolean
  children?: MenuItem[]
  permissions?: MenuPermission[]
}

export interface TreeItem {
  tag: string
  labelKey: string
  permissionKey?: string
  children?: TreeItem[]
}

export interface BackPermission {
  tag: string
  type: ItemType
  permissionKey?: string
  isWhiteList?: boolean
  method?: HttpMethod | null
  pattern?: string
}
