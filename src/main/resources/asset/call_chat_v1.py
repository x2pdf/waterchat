import torch
import time
import json
import sys
from transformers import AutoModelForCausalLM, AutoTokenizer

def load_messages_from_file(file_path):
    """从指定的 JSON 文件路径加载消息列表"""
    try:
        with open(file_path, 'rb') as file:
            messages = json.load(file)
            if not isinstance(messages, list) or not all(isinstance(msg, dict) for msg in messages):
                raise ValueError("Error: 'messages' must be a list of dictionaries.")
            return messages
    except (IOError, json.JSONDecodeError, ValueError) as e:
        print(f"Error: {e}")
        sys.exit(1)


def read_config(file_path):
    config_dict = {}
    try:
        with open(file_path, 'r') as f:
          for line in f:
            line = line.strip()
            if '=' in line:
              key, value = line.split('=', 1)
              config_dict[key] = value
        return config_dict
    except FileNotFoundError:
        print("config_path does not exist.")
        return config_dict
        
        
def get_torch_type(torch_type_str):
    if "" == torch_type_str:
         return "auto"
    if "torch.float16" == torch_type_str:
        return torch.float16
    if "torch.float32" == torch_type_str:
         return torch.float32
    if "torch.int8" == torch_type_str:
        return torch.int8
    if "torch.int16" == torch_type_str:
        return torch.int16
    if "torch.int32" == torch_type_str:
        return torch.int32         
    if "torch.int64" == torch_type_str:
        return torch.int64
    else:
        return "auto"

def print_config(config):
    if config:  # 判断字典是否为空
        for key, value in config.items():
            print(f"{key} = {value}")

def main():
    # 检测是否支持 CUDA 加速，如果支持则使用 'cuda'，否则使用 'cpu'
    device = "cuda" if torch.cuda.is_available() else "cpu"
    print(f"Using device: {device}")

    # 从命令行读取外部传入的 JSON 文件路径参数
    if len(sys.argv) > 1:
        messages_file_path = sys.argv[1]
        messages = load_messages_from_file(messages_file_path)
    else:
        print("Error: No messages file path provided.")
        sys.exit(1)
        
    if len(sys.argv) > 2:
        config_path = sys.argv[2]
        config = read_config(config_path)
        print(f"config: ")
        print_config(config)
        print(f" ")
    else:
        print("Error: No config_path provided.")
        
    if len(sys.argv) > 3:
        response_path = sys.argv[3]
    else:
        print("Error: No response_path provided.")

    # 模型路径作为一个可变参数
    model_path = config.get('model_path')  # 可以将该路径改为其他路径 TODO
    
    # 加载模型和分词器
    torch_dtype_config = get_torch_type(config.get('torch_dtype'))
    print(f"torch_dtype: {torch_dtype_config}")
    model = AutoModelForCausalLM.from_pretrained(
        model_path,  # 从可变路径加载模型
        torch_dtype=torch_dtype_config,
        device_map="auto" if device == "cuda" else None  # 自动分配设备映射，仅在使用 GPU 时启用
    )
    tokenizer = AutoTokenizer.from_pretrained(model_path)

    # 处理 messages 生成输入文本
    text = tokenizer.apply_chat_template(
        messages,
        tokenize=False,
        add_generation_prompt=True
    )
    model_inputs = tokenizer([text], return_tensors="pt").to(device)

    # 生成响应
    generated_ids = model.generate(
        model_inputs.input_ids,
        max_new_tokens=int(config.get('max_new_tokens', '512'))
    )


    generated_ids = [
        output_ids[len(input_ids):] for input_ids, output_ids in zip(model_inputs.input_ids, generated_ids)
    ]

    response = tokenizer.batch_decode(generated_ids, skip_special_tokens=True)[0]
    
    with open(response_path, 'w', encoding='utf-8') as f:
        f.write(response)


if __name__ == "__main__":
    start_time = time.time()
    main()
    end_time = time.time()
    print("call python chat spend(s):", end_time - start_time)
    print("====== out main():" + time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(time.time())))

