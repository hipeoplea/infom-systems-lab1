<template>
  <div class="modal">
    <div class="card">
      <h3>{{ mode === "create" ? `Create ${title}` : `Edit ${title}` }}</h3>

      <div class="form-grid">
        <div
          v-for="f in fields"
          :key="f.key"
          class="form-field"
          :style="f.full ? 'grid-column:1/3' : ''"
        >
          <label>{{ f.label }}</label>
          <template v-if="f.type === 'select'">
            <select v-model="form[f.key]" class="input">
              <option v-if="f.nullable" :value="null">--</option>
              <option
                v-for="opt in f.options || []"
                :key="isObj(opt) ? opt.value : opt"
                :value="isObj(opt) ? opt.value : opt"
              >
                {{ isObj(opt) ? opt.label : opt }}
              </option>
            </select>
          </template>
          <template v-else>
            <input
              class="input"
              :type="f.type || 'text'"
              :value="form[f.key]"
              @input="
                (e) =>
                  (form[f.key] = isNumber(f.type)
                    ? toNumber(e.target.value)
                    : e.target.value)
              "
            />
          </template>
          <div v-if="errors[f.key]" class="error">{{ errors[f.key] }}</div>
        </div>
      </div>

      <div
        class="mt-12"
        style="display: flex; gap: 8px; justify-content: flex-end"
      >
        <button class="btn btn-primary" @click="submit">Save</button>
        <button class="btn btn-secondary" @click="$emit('close')">Close</button>
      </div>

      <div v-if="serverError" class="error" style="margin-top: 8px">
        {{ serverError }}
      </div>
    </div>
  </div>
</template>

<script>
import { ref, watch } from "vue";
import { getOne, createOne, updateOne } from "../api";

export default {
  props: {
    resource: { type: String, required: true },
    title: { type: String, default: "" },
    fields: { type: Array, required: true },
    mode: { type: String, default: "create" },
    id: { type: Number, default: null },
    toPayload: { type: Function, default: (v) => v },
    fromEntity: { type: Function, default: (v) => v },
  },
  emits: ["saved", "close", "error"],
  setup(props, { emit }) {
    const form = ref({});
    const errors = ref({});
    const serverError = ref(null);

    const defaults = () =>
      Object.fromEntries(props.fields.map((f) => [f.key, f.default ?? null]));
    form.value = defaults();

    const load = async () => {
      if (props.mode === "edit" && props.id) {
        try {
          form.value = props.fromEntity(await getOne(props.resource, props.id));
        } catch (e) {
          emit("error", e.response?.data || e.message);
        }
      } else {
        form.value = defaults();
      }
    };
    watch(() => props.id, load, { immediate: true });

    const validate = () => {
      errors.value = {};
      for (const f of props.fields) {
        if (
          f.required &&
          (form.value[f.key] === null ||
            form.value[f.key] === "" ||
            typeof form.value[f.key] === "undefined")
        ) {
          errors.value[f.key] = "This field is required";
        }
      }
      return Object.keys(errors.value).length === 0;
    };

    const submit = async () => {
      serverError.value = null;
      if (!validate()) return;
      try {
        const payload = props.toPayload(form.value);
        if (props.mode === "create") await createOne(props.resource, payload);
        else await updateOne(props.resource, props.id, payload);
        emit("saved");
      } catch (e) {
        serverError.value = e.response?.data?.message || e.message;
      }
    };

    const isNumber = (t) => t === "number";
    const toNumber = (v) => (v === "" ? null : Number(v));
    const isObj = (v) => v != null && typeof v === "object";

    return { form, errors, serverError, submit, isNumber, toNumber, isObj };
  },
};
</script>
