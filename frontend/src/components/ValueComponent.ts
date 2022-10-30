export interface ValueComponent<T> {
    onChange(callback: (data: T) => void): void;
    setValue(value: T): void;
    getValue(): T;
}